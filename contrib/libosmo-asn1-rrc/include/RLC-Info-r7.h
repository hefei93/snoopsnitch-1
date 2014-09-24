/*
 * Generated by asn1c-0.9.24 (http://lionet.info/asn1c)
 * From ASN.1 module "InformationElements"
 * 	found in "../asn/InformationElements.asn"
 * 	`asn1c -fcompound-names -fnative-types`
 */

#ifndef	_RLC_Info_r7_H_
#define	_RLC_Info_r7_H_


#include <asn_application.h>

/* Including external dependencies */
#include <BOOLEAN.h>
#include <NativeEnumerated.h>
#include <constr_SEQUENCE.h>

#ifdef __cplusplus
extern "C" {
#endif

/* Dependencies */
typedef enum RLC_Info_r7__altE_bitInterpretation {
	RLC_Info_r7__altE_bitInterpretation_true	= 0
} e_RLC_Info_r7__altE_bitInterpretation;
typedef enum RLC_Info_r7__useSpecialValueOfHEField {
	RLC_Info_r7__useSpecialValueOfHEField_true	= 0
} e_RLC_Info_r7__useSpecialValueOfHEField;

/* Forward declarations */
struct UL_RLC_Mode;
struct DL_RLC_Mode_r7;

/* RLC-Info-r7 */
typedef struct RLC_Info_r7 {
	struct UL_RLC_Mode	*ul_RLC_Mode	/* OPTIONAL */;
	struct DL_RLC_Mode_r7	*dl_RLC_Mode	/* OPTIONAL */;
	BOOLEAN_t	 rlc_OneSidedReEst;
	long	*altE_bitInterpretation	/* OPTIONAL */;
	long	*useSpecialValueOfHEField	/* OPTIONAL */;
	
	/* Context for parsing across buffer boundaries */
	asn_struct_ctx_t _asn_ctx;
} RLC_Info_r7_t;

/* Implementation */
/* extern asn_TYPE_descriptor_t asn_DEF_altE_bitInterpretation_5;	// (Use -fall-defs-global to expose) */
/* extern asn_TYPE_descriptor_t asn_DEF_useSpecialValueOfHEField_7;	// (Use -fall-defs-global to expose) */
extern asn_TYPE_descriptor_t asn_DEF_RLC_Info_r7;

#ifdef __cplusplus
}
#endif

/* Referred external types */
#include "UL-RLC-Mode.h"
#include "DL-RLC-Mode-r7.h"

#endif	/* _RLC_Info_r7_H_ */
#include <asn_internal.h>