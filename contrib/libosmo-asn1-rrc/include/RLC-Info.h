/*
 * Generated by asn1c-0.9.24 (http://lionet.info/asn1c)
 * From ASN.1 module "InformationElements"
 * 	found in "../asn/InformationElements.asn"
 * 	`asn1c -fcompound-names -fnative-types`
 */

#ifndef	_RLC_Info_H_
#define	_RLC_Info_H_


#include <asn_application.h>

/* Including external dependencies */
#include <constr_SEQUENCE.h>

#ifdef __cplusplus
extern "C" {
#endif

/* Forward declarations */
struct UL_RLC_Mode;
struct DL_RLC_Mode;

/* RLC-Info */
typedef struct RLC_Info {
	struct UL_RLC_Mode	*ul_RLC_Mode	/* OPTIONAL */;
	struct DL_RLC_Mode	*dl_RLC_Mode	/* OPTIONAL */;
	
	/* Context for parsing across buffer boundaries */
	asn_struct_ctx_t _asn_ctx;
} RLC_Info_t;

/* Implementation */
extern asn_TYPE_descriptor_t asn_DEF_RLC_Info;

#ifdef __cplusplus
}
#endif

/* Referred external types */
#include "UL-RLC-Mode.h"
#include "DL-RLC-Mode.h"

#endif	/* _RLC_Info_H_ */
#include <asn_internal.h>