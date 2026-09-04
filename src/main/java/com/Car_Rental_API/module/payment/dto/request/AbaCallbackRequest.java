package com.Car_Rental_API.module.payment.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// * ABA PayWay callback incoming payload
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbaCallbackRequest {
	@Schema(description = "ABA PayWay transaction ID", example = "20260629120000001")
	@JsonProperty("tran_id")
	private String tranId;

	@Schema(description = "Approval code from ABA", example = "123456")
	private String apv;

	@Schema(description = "Payment status string (APPROVED, FAILED, etc.)", example = "APPROVED")
	private String status;

	@Schema(description = "Return parameters echoed by PayWay", example = "26SO000001")
	@JsonProperty("return_params")
	private String returnParams;
}
