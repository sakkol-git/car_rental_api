package com.Car_Rental_API.module.sale_order.dto.response;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileBookingCreateResponse {

	@Schema(description = "Transaction ID / Order No")
	private String transactionId;

	@JsonProperty("abapay_deeplink")
	@Schema(description = "ABA Pay app deep link URL")
	private String abapayDeeplink;

	@JsonProperty("checkout_qr_url")
	@Schema(description = "ABA Web checkout QR page URL")
	private String checkoutQrUrl;

	@Schema(description = "ABA KHQR code string")
	private String qrString;

	@Schema(description = "Base64-encoded QR image")
	private String qrImage;

	@Schema(description = "Total amount to pay")
	private BigDecimal totalAmount;

	@Schema(description = "Currency code (USD/KHR)")
	private String currency;

	@Schema(description = "Payment status code (1: Pending, 2: Paid, 3: Failed)")
	private Integer paymentStatus;

	@Schema(description = "Payment status label (PENDING, APPROVED, FAILED)")
	private String paymentStatusLabel;
}
