package de.srlabs.msd.qdmon;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.GZIPOutputStream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import de.srlabs.msd.util.MsdLog;

public class EncryptedFileWriter{
	public static final String TAG = "msd-EncryptedFileWriter";
	private String encryptedFilename;
	private boolean compressEncryptedFile;
	private boolean closed = false;
	private MsdService msdService;
	private Process openssl;
	private OutputStream encryptedOutputStream;
	private String plaintextFilename;
	private BufferedReader opensslStderr;
	private OpensslErrorThread opensslErrorThread;
	private BlockingQueue<MsgWrapper> msgQueue = new LinkedBlockingQueue<MsgWrapper>();
	private OutputStream plaintextOutputStream;
	private boolean compressPlaintextFile;
	private WriterThread writerThread;
	private long lastWriteTime = 0;
	private long lastFlushTime = 0;
	public EncryptedFileWriter(MsdService msdService, String encryptedFilename, boolean compressEncryptedFile, String plaintextFilename, boolean compressPlaintextFile) {
		this.msdService = msdService;
		this.encryptedFilename = encryptedFilename;
		this.compressEncryptedFile = compressEncryptedFile;
		this.plaintextFilename = plaintextFilename;
		this.compressPlaintextFile = compressPlaintextFile;
		openOutput();
	}
	@SuppressLint("NewApi")
	private void openOutput() {
		if(encryptedFilename != null){
			info("Writing encrypted output to " + encryptedFilename);
			String libdir = msdService.getApplicationInfo().nativeLibraryDir;
			String openssl_binary = libdir + "/libopenssl.so";
			String crtFile = libdir + "/libsmime_crt.so";
			String cmd[] = {openssl_binary, "smime", "-encrypt", "-binary", "-aes256", "-outform", "DER", "-out", msdService.getFilesDir() + "/" + encryptedFilename, crtFile};
			String env[] = {"LD_LIBRARY_PATH=" + libdir, "OPENSSL_CONF=/dev/null", "RANDFILE=/dev/null"};
			info("Launching openssl: " + TextUtils.join(" ",cmd));
			try {
				openssl =  Runtime.getRuntime().exec(cmd, env, null);
			} catch (IOException e) {
				msdService.handleFatalError("IOException while launching openssl for file" + encryptedFilename,e);
			}
			encryptedOutputStream = openssl.getOutputStream();
			if(compressEncryptedFile){
				try {
					encryptedOutputStream = new GZIPOutputStream(encryptedOutputStream);
				} catch (IOException e) {
					msdService.handleFatalError("IOException while opening GZIPOutputStream in EncryptedFileWrite.openOutput, file=" + encryptedFilename);
				}
			}
			opensslStderr = new BufferedReader(new InputStreamReader(openssl.getErrorStream()));
			opensslErrorThread = new OpensslErrorThread();
			opensslErrorThread.start();
		}
		if(plaintextFilename != null){
			try {
				plaintextOutputStream = msdService.openFileOutput(plaintextFilename, Context.MODE_APPEND);
				if(compressPlaintextFile){
					plaintextOutputStream = new GZIPOutputStream(plaintextOutputStream,true); // TODO: Disable syncMode again later
				}
			} catch (IOException e) {
				msdService.handleFatalError("FileNotFoundException while opening plaintext output in EncryptedFileWrite.openOutput, file=" + encryptedFilename);
			}
		}
		writerThread = new WriterThread();
		writerThread.start();
		lastFlushTime = System.currentTimeMillis();
		closed = false;
	}
	class MsgWrapper{
		byte[] buf;
		public MsgWrapper(byte[] buf){
			this.buf = buf;
		}
	}
	class ShutdownMsgWrapper extends MsgWrapper{
		public ShutdownMsgWrapper() {
			super(null);
		}
	}
	class FlushMsgWrapper extends MsgWrapper{
		public Object markerReached = new Object();
		public Object flushDone = new Object();
		public FlushMsgWrapper() {
			super(null);
		}
	}
	class OpensslErrorThread extends Thread{
		private boolean closeOutputRunning;
		@Override
		public void run() {
			try {
				while(true){
					String line = opensslStderr.readLine();
					if(line == null){
						if(closeOutputRunning){
							info("opensslStderr.readLine() returned null while closeOutputRunning is set, OK");
						} else{
							msdService.handleFatalError("opensslStderr.readLine() returned null for file " + encryptedFilename);
						}
						return;
					}
					msdService.handleFatalError("Openssl Error for " + encryptedFilename + ": " + line);
				}
			} catch(EOFException e){
				if(closeOutputRunning){
					info("OpensslErrorThread received IOException while shutting down, OK");
				} else{
					msdService.handleFatalError("EOFException while reading from opensslStderr for " + encryptedFilename + ": " + e.getMessage());
				}
			} catch(IOException e){
				msdService.handleFatalError("IOException while reading from opensslStderr for " + encryptedFilename + ": " + e.getMessage());
			}
		}
	}
	class WriterThread extends Thread{
		@Override
		public void run() {
			try {
				while(true){
					MsgWrapper msg = msgQueue.take();
					if(msg instanceof ShutdownMsgWrapper){
						return;
					}
					if(msg instanceof FlushMsgWrapper){
						FlushMsgWrapper flushMsg = (FlushMsgWrapper)msg;
						synchronized(flushMsg.flushDone){
							synchronized(flushMsg.markerReached){
								flushMsg.markerReached.notify();
							}
							flushMsg.flushDone.wait();
						}
					} else{
						if(encryptedOutputStream != null){
							encryptedOutputStream.write(msg.buf);
						}
						if(plaintextOutputStream != null){
							plaintextOutputStream.write(msg.buf);
						}
					}
				}
			} catch (InterruptedException e) {
				msdService.handleFatalError("EncryptedFileWriter.WriterThread shutting down due to InterruptedException, file=" + encryptedFilename, e);
			} catch (IOException e) {
				close();
				msdService.handleFatalError("EncryptedFileWriter.WriterThread: IOException, file=" + encryptedFilename, e);
			}
		}
	}
	private void info(String msg){
		MsdLog.i(TAG + ":" + encryptedFilename,msg);
	}
	public synchronized void write(byte[] data){
		if(closed){
			throw new IllegalStateException("Can't write data, EncrypteFileWriter is already closed");
		}
		lastWriteTime = System.currentTimeMillis();
		msgQueue.add(new MsgWrapper(data));
	}
	public synchronized void write(String str){
		write(str.getBytes());
	}
	public synchronized void close(){
		closed = true;
		// Send shutdown marker
		msgQueue.add(new ShutdownMsgWrapper());
		try{
			writerThread.join(3000);
			if(writerThread.isAlive()){
				msdService.handleFatalError("EncryptedFileWriter.close() failed to stop writerThread for file " + encryptedFilename);
			}
			writerThread.join();
			writerThread = null;
			opensslErrorThread.closeOutputRunning = true;
			if(encryptedOutputStream != null)
				encryptedOutputStream.close();
			encryptedOutputStream = null;
			if(plaintextOutputStream != null)
				plaintextOutputStream.close();
			plaintextOutputStream = null;
			if(openssl != null){
				info("Waiting for openssl to terminate during close()");
				Thread t = new Thread(){
					public void run() {
						try{
							openssl.waitFor();
						} catch(InterruptedException e){
						}
					};
				};
				t.start();
				t.join(3000);
				t.interrupt();
				try{
					int exitValue = openssl.exitValue();
					info("openssl terminated with exit value " + exitValue);
				} catch(IllegalThreadStateException e){
					msdService.handleFatalError("EncryptedFileWriter.close() for file " + encryptedFilename + " failed to stop parser, calling destroy(): " + e.getMessage());
					openssl.destroy();
				}
				openssl = null;
			}
			if(opensslErrorThread != null){
				opensslErrorThread.join(3000);
				if(opensslErrorThread.isAlive()){
					msdService.handleFatalError("EncryptedFileWriter.close() for file " + encryptedFilename + " failed to stop opensslErrorThread");
				}
				opensslErrorThread.join();
				opensslErrorThread = null;
			}
		} catch(InterruptedException e){
			msdService.handleFatalError("InterruptedException in EncryptedFileWriter.close() for file " + encryptedFilename,e);
		} catch (IOException e1) {
			msdService.handleFatalError("IOException in EncryptedFileWriter.close() for file " + encryptedFilename,e1);
		}
	}
	public synchronized void flush(){
		lastFlushTime = System.currentTimeMillis();
		info("EncryptedFileWriter.flush called, queue size=" + getQueueSize());
		try{
			// Add a marker to the message queue and wait until the marker (and all messages before it) has been reached
			FlushMsgWrapper flushMsg = new FlushMsgWrapper();
			synchronized(flushMsg.markerReached){
				msgQueue.add(flushMsg);
				flushMsg.markerReached.wait();
			}
			if(encryptedOutputStream != null)
				encryptedOutputStream.flush();
			if(compressPlaintextFile){
				// GZIPOutputStream doesn't allow reliable flushing of output, so let's just reopen the file with MODE_APPEND, gzip files can be concatenated.
				plaintextOutputStream.close();
				plaintextOutputStream = msdService.openFileOutput(plaintextFilename, Context.MODE_APPEND);
				if(compressPlaintextFile){
					plaintextOutputStream = new GZIPOutputStream(plaintextOutputStream);
				}
			} else{
				if(plaintextOutputStream != null)
					plaintextOutputStream.flush();
			}
			synchronized (flushMsg.flushDone) {
				flushMsg.flushDone.notifyAll();
			}
			info("EncryptedFileWriter.flush done");
		} catch(IOException e){
			msdService.handleFatalError("IOException in EncryptedFileWriter.flush(), file=" + encryptedFilename,e);
		} catch(Exception e){
			msdService.handleFatalError("Exception in EncryptedFileWriter.flush(), file=" + encryptedFilename,e);
		}
	}
	public synchronized void flushIfUnflushedDataSince(long millis){
		if(lastWriteTime == 0)
			return;
		if(lastWriteTime > lastFlushTime + millis){
			flush();
		}
	}
	public int getQueueSize(){
		return msgQueue.size();
	}
//	public void reopen(String encryptedFilename, String plaintextFilename){
//		info("reopen(" + encryptedFilename + ", " + plaintextFilename);
//		reopenRunning = true;
//		close();
//		this.encryptedFilename = encryptedFilename;
//		this.plaintextFilename = plaintextFilename;
//		openOutput();
//		reopenRunning = false;
//	}
	public String getEncryptedFilename() {
		return encryptedFilename;
	}
	public String getPlaintextFilename() {
		return plaintextFilename;
	}
}
