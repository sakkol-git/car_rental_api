package com.Car_Rental_API.module.payment.dto.response;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// * Response payload for payment status or initiation
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {
	@Schema(description = "ABA PayWay transaction ID")
	private String transactionId;

	@Schema(description = "QR code string for scanning")
	private String qrString;

	@Schema(description = "Base64-encoded QR image")
	private String qrImage;

	@JsonProperty("abapay_deeplink")
	@Schema(description = "ABA Pay deep-link url")
	private String abapayUrl;

	@Schema(description = "App Store link")
	private String appStore;

	@Schema(description = "Play Store link")
	private String playStore;

	@Schema(description = "Payment status: 1=Pending, 2=Paid, 3=Fail, 4=Expired")
	private Integer paymentStatus;

	@Schema(description = "Payment status label (APPROVED, FAILED, etc.)")
	private String paymentStatusLabel;

	@Schema(description = "Amount charged")
	private BigDecimal amount;

	@Schema(description = "Currency code")
	private String currency;

	@Schema(description = "Human-readable message")
	private String message;

	@Schema(description = "Remaining seconds before QR expiration")
	private Long expirySeconds;

	@JsonProperty("checkout_qr_url")
	@Schema(description = "ABA-hosted checkout QR page URL")
	private String checkoutQrUrl;
}
