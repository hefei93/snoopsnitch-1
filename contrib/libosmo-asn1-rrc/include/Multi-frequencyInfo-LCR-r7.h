/*
 * Generated by asn1c-0.9.24 (http://lionet.info/asn1c)
 * From ASN.1 module "InformationElements"
 * 	found in "../asn/InformationElements.asn"
 * 	`asn1c -fcompound-names -fnative-types`
 */

#ifndef	_Multi_frequencyInfo_LCR_r7_H_
#define	_Multi_frequencyInfo_LCR_r7_H_


#include <asn_application.h>

/* Including external dependencies */
#include "UpPCHposition-LCR.h"
#include <constr_SEQUENCE.h>

#ifdef __cplusplus
extern "C" {
#endif

/* Forward declarations */
struct FrequencyInfoTDD;

/* Multi-frequencyInfo-LCR-r7 */
typedef struct Multi_frequencyInfo_LCR_r7 {
	struct FrequencyInfoTDD	*secondFrequencyInfo	/* OPTIONAL */;
	struct FrequencyInfoTDD	*fPachFrequencyInfo	/* OPTIONAL */;
	UpPCHposition_LCR_t	*upPCHpositionInfo	/* OPTIONAL */;
	
	/* Context for parsing across buffer boundaries */
	asn_struct_ctx_t _asn_ctx;
} Multi_frequencyInfo_LCR_r7_t;

/* Implementation */
extern asn_TYPE_descriptor_t asn_DEF_Multi_frequencyInfo_LCR_r7;

#ifdef __cplusplus
}
#endif

/* Referred external types */
#include "FrequencyInfoTDD.h"

#endif	/* _Multi_frequencyInfo_LCR_r7_H_ */
#include <asn_internal.h>
