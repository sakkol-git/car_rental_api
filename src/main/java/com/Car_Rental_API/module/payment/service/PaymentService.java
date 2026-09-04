package com.Car_Rental_API.module.payment.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.Car_Rental_API.module.payment.dto.request.AbaCallbackRequest;
import com.Car_Rental_API.module.payment.dto.response.PaymentResponse;
import com.Car_Rental_API.module.sale_order.repository.SaleOrderRepository;
import com.Car_Rental_API.module.sale_order.model.SaleOrder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// * Business service for managing ABA PayWay payments and webhooks
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

	private final AbaPaywayService abaPaywayService;
	private final SaleOrderRepository saleOrderRepository;
	private final ObjectMapper objectMapper;

	// * Handle webhook callback from ABA PayWay
	@Transactional
	public PaymentResponse handleCallback(AbaCallbackRequest callbackRequest, String rawPayload) {
		String tranId = callbackRequest != null ? callbackRequest.getTranId() : null;
		if (tranId == null || tranId.isBlank()) {
			tranId = extractTranId(rawPayload);
		}
		if (tranId == null || tranId.isBlank()) {
			log.warn("[handleCallback] Missing tran_id in callback payload");
			return errorResponse("Invalid callback payload", 0);
		}
		return processPaymentStatus(tranId);
	}

	// * Verify transaction payment status directly with ABA PayWay API
	@Transactional
	public PaymentResponse checkPaymentStatus(String transactionId) {
		if (transactionId == null || transactionId.isBlank()) {
			return errorResponse("Transaction ID is required", 0);
		}
		return processPaymentStatus(transactionId);
	}

	// * Cancel pending payment transaction
	@Transactional
	public PaymentResponse cancelPayment(String transactionId) {
		if (transactionId == null || transactionId.isBlank()) {
			return errorResponse("Transaction ID is required", 0);
		}
		try {
			SaleOrder order = saleOrderRepository.findByOrderNo(transactionId)
					.orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

			saleOrderRepository.updateOrderStatus(order.getId(), (byte) 5); // 5 = REJECTED / CANCELLED
			return PaymentResponse.builder()
					.transactionId(transactionId)
					.paymentStatus(3)
					.paymentStatusLabel("FAILED")
					.message("Transaction cancelled successfully")
					.build();
		} catch (Exception e) {
			log.error("[cancelPayment] Failed to cancel transaction: {}", transactionId, e);
			return errorResponse("Failed to cancel payment: " + e.getMessage(), 0);
		}
	}

	// * Check payment QR details for frontend web view
	public PaymentResponse findQrPayment(String transactionId) {
		return findQrPayment(transactionId, "abapay_khqr_deeplink");
	}

	public PaymentResponse findQrPaymentForOrder(SaleOrder order, String tranId, Integer paymentOption) {
		if (order == null) {
			return errorResponse("Order is required", 0);
		}
		String transactionId = (tranId != null && !tranId.isBlank()) ? tranId : generateTransactionId();
		if (paymentOption != null && paymentOption == 2) { // 2 = COD
			return PaymentResponse.builder()
					.transactionId(transactionId)
					.paymentStatus(1)
					.paymentStatusLabel("PENDING_COD")
					.message("Cash on Delivery selected")
					.build();
		}
		try {
			BigDecimal amount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.TEN;
			String currency = (order.getCurrency() != null && !order.getCurrency().isBlank()) ? order.getCurrency() : "USD";
			PaymentResponse response = abaPaywayService.generatePurchaseQr(transactionId, amount, currency, paymentOption);
			if (response != null) {
				response.setAmount(amount);
				response.setCurrency(currency);
				if (response.getTransactionId() == null) {
					response.setTransactionId(transactionId);
				}
			}
			return response;
		} catch (Exception e) {
			log.error("[findQrPaymentForOrder] Failed for orderNo={}, tranId={}: {}", order.getOrderNo(), transactionId, e.getMessage());
			return errorResponse("Failed to generate QR payment: " + e.getMessage(), 0);
		}
	}

	public String generateTransactionId() {
		String ts = new java.text.SimpleDateFormat("yyyyMMddHHmmssSSS").format(new java.util.Date());
		int rnd = new java.util.Random().nextInt(900) + 100;
		String candidate = ts + rnd;
		return candidate.length() > 20 ? candidate.substring(0, 20) : candidate;
	}

	public PaymentResponse findQrPayment(String transactionId, Integer paymentOption) {
		if (transactionId == null || transactionId.isBlank()) {
			return errorResponse("Transaction ID is required", 0);
		}
		if (paymentOption != null && paymentOption == 2) { // 2 = COD
			return PaymentResponse.builder()
					.transactionId(transactionId)
					.paymentStatus(1)
					.paymentStatusLabel("PENDING_COD")
					.message("Cash on Delivery selected")
					.build();
		}
		try {
			SaleOrder order = saleOrderRepository.findByOrderNo(transactionId)
					.orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

			BigDecimal amount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.TEN;
			String currency = order.getCurrency() != null ? order.getCurrency() : "USD";
			PaymentResponse response = abaPaywayService.generatePurchaseQr(transactionId, amount, currency, paymentOption);
			if (response != null) {
				response.setAmount(amount);
				response.setCurrency(currency);
			}
			return response;
		} catch (Exception e) {
			log.error("[findQrPayment] Failed for transaction: {}", transactionId, e);
			return errorResponse("Failed to find QR payment: " + e.getMessage(), 0);
		}
	}

	public PaymentResponse findQrPayment(String transactionId, String paymentOption) {
		if (transactionId == null || transactionId.isBlank()) {
			return errorResponse("Transaction ID is required", 0);
		}
		try {
			SaleOrder order = saleOrderRepository.findByOrderNo(transactionId)
					.orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

			BigDecimal amount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.TEN;
			String currency = order.getCurrency() != null ? order.getCurrency() : "USD";
			PaymentResponse response = abaPaywayService.generatePurchaseQr(transactionId, amount, currency, paymentOption);
			if (response != null) {
				response.setAmount(amount);
				response.setCurrency(currency);
			}
			return response;
		} catch (Exception e) {
			log.error("[findQrPayment] Failed for transaction: {}", transactionId, e);
			return errorResponse("Failed to find QR payment: " + e.getMessage(), 0);
		}
	}

	// * Core logic to check transaction status with ABA and update database
	private PaymentResponse processPaymentStatus(String transactionId) {
		try {
			AbaPaywayService.CheckTransactionResult result = abaPaywayService.checkTransaction(transactionId);
			String payStatus = result.paymentStatus();
			Integer code = result.paymentStatusCode();

			int statusInt = 1; // Default Pending
			String label = "PENDING";

			if ("APPROVED".equalsIgnoreCase(payStatus) || (code != null && code == 0)) {
				statusInt = 2; // Paid
				label = "APPROVED";
			} else if ("DECLINED".equalsIgnoreCase(payStatus) || "FAILED".equalsIgnoreCase(payStatus) || (code != null && code != 0 && code != 1)) {
				statusInt = 3; // Failed
				label = "FAILED";
			}

			final int statusIntFinal = statusInt;
			saleOrderRepository.findByOrderNo(transactionId).ifPresent(order -> {
				if (statusIntFinal == 2) { // Approved / Paid
					BigDecimal total = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
					BigDecimal existingPaid = order.getPaidAmount() != null ? order.getPaidAmount() : BigDecimal.ZERO;
					BigDecimal stepAmount = total.subtract(existingPaid).max(BigDecimal.ZERO);
					saleOrderRepository.updateReceipt(order.getId(),
							order.getPaymentType() != null ? order.getPaymentType() : (byte) 1,
							(byte) 3, // paymentStatus = 3 (Paid)
							order.getPaymentMethod(),
							total, // paidAmount = total
							order.getDepositPrice(),
							order.getDiscountAmount(),
							total,
							BigDecimal.ZERO, // remainingAmount = 0
							order.getReceiptFileName(),
							order.getReceiptFileUrl(),
							order.getReceiptDescription(),
							stepAmount,
							order.getTotalAmount(),
							null);
				}
			});

			return PaymentResponse.builder()
					.transactionId(transactionId)
					.paymentStatus(statusInt)
					.paymentStatusLabel(label)
					.message("Transaction status: " + label)
					.build();
		} catch (Exception e) {
			log.error("[processPaymentStatus] Exception checking transaction: {}", transactionId, e);
			return errorResponse("Error processing payment status: " + e.getMessage(), 0);
		}
	}

	private String extractTranId(String rawPayload) {
		if (rawPayload == null || rawPayload.isBlank()) return null;
		try {
			JsonNode root = objectMapper.readTree(rawPayload);
			return root.path("tran_id").asText(null);
		} catch (Exception e) {
			return null;
		}
	}

	private PaymentResponse errorResponse(String msg, int code) {
		return PaymentResponse.builder()
				.paymentStatus(code)
				.paymentStatusLabel("ERROR")
				.message(msg)
				.build();
	}
}
