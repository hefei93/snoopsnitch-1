/*
 * Generated by asn1c-0.9.24 (http://lionet.info/asn1c)
 * From ASN.1 module "InformationElements"
 * 	found in "../asn/InformationElements.asn"
 * 	`asn1c -fcompound-names -fnative-types`
 */

#ifndef	_TimeslotList_H_
#define	_TimeslotList_H_


#include <asn_application.h>

/* Including external dependencies */
#include "TimeslotNumber.h"
#include <asn_SEQUENCE_OF.h>
#include <constr_SEQUENCE_OF.h>

#ifdef __cplusplus
extern "C" {
#endif

/* TimeslotList */
typedef struct TimeslotList {
	A_SEQUENCE_OF(TimeslotNumber_t) list;
	
	/* Context for parsing across buffer boundaries */
	asn_struct_ctx_t _asn_ctx;
} TimeslotList_t;

/* Implementation */
extern asn_TYPE_descriptor_t asn_DEF_TimeslotList;

#ifdef __cplusplus
}
#endif

#endif	/* _TimeslotList_H_ */
#include <asn_internal.h>
