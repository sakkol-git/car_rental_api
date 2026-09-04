package com.Car_Rental_API.module.payment.controller;


import com.Car_Rental_API.module.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.Car_Rental_API.common.base.BaseController;
import com.Car_Rental_API.common.base_dto.response.BaseResponse;
import com.Car_Rental_API.module.payment.dto.request.AbaCallbackRequest;
import com.Car_Rental_API.module.payment.dto.response.PaymentResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// * Controller mapping to payment status webhook and verification endpoints
@Slf4j
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Tag(name = "27. ABA Payment", description = "Payment verification & ABA PayWay resources")
public class PaymentController extends BaseController {

	private final PaymentService paymentService;
	private final ObjectMapper objectMapper;

	@PostMapping("/payway/callback")
	@Operation(summary = "ABA PayWay callback webhook")
	public ResponseEntity<BaseResponse<PaymentResponse>> paywayCallback(@RequestBody String payload, HttpServletRequest httpRequest) {
		AbaCallbackRequest callbackRequest = null;
		try {
			callbackRequest = objectMapper.readValue(payload, AbaCallbackRequest.class);
		} catch (Exception e) {
			log.warn("[paywayCallback] Failed to parse payload: {}", e.getMessage());
		}

		return success(paymentService.handleCallback(callbackRequest, payload));
	}

	@PostMapping("/cancel/{transactionId}")
	@Operation(summary = "Cancel pending payment transaction")
	public ResponseEntity<BaseResponse<PaymentResponse>> cancelSaleOrder(@PathVariable("transactionId") String transactionId) {
		return success(paymentService.cancelPayment(transactionId));
	}

	@PostMapping("/check/{transactionId}")
	@Operation(summary = "Verify payment status with ABA API")
	public ResponseEntity<BaseResponse<PaymentResponse>> checkTransaction(@PathVariable("transactionId") String transactionId) {
		return success(paymentService.checkPaymentStatus(transactionId));
	}

	@PostMapping("/find-qr-payment/{transactionId}")
	@Operation(summary = "Check payment QR details for frontend web view")
	public ResponseEntity<BaseResponse<PaymentResponse>> findQrPayment(@PathVariable("transactionId") String transactionId) {
		return success(paymentService.findQrPayment(transactionId));
	}
}
