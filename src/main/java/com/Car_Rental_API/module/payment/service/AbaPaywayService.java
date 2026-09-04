package com.Car_Rental_API.module.payment.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.Car_Rental_API.module.payment.config.AbaProperties;
import com.Car_Rental_API.module.payment.dto.response.PaymentResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// * Service class to integrate with ABA PayWay API endpoints
@Slf4j
@Service
@RequiredArgsConstructor
public class AbaPaywayService {

	private final AbaProperties properties;
	private final ObjectMapper objectMapper;
	private final HttpClient httpClient = HttpClient.newHttpClient();

	@Value("${spring.profiles.active:dev}")
	private String activeProfile;

	public record CheckTransactionResult(
			String paymentStatus,
			Integer paymentStatusCode,
			String apv,
			Double totalAmount
	) {}

	// * Generate ABA Purchase QR code (standard checkout flow)
	public PaymentResponse generatePurchaseQr(String tranId, BigDecimal amount, String currency) throws Exception {
		return generatePurchaseQr(tranId, amount, currency, "abapay_khqr_deeplink");
	}

	public PaymentResponse generatePurchaseQr(String tranId, BigDecimal amount, String currency, Integer paymentOption) throws Exception {
		String option = "abapay_khqr_deeplink";
		if (paymentOption != null && paymentOption == 3) {
			option = "acleda";
		}
		return generatePurchaseQr(tranId, amount, currency, option);
	}

	public PaymentResponse generatePurchaseQr(String tranId, BigDecimal amount, String currency, String paymentOption) throws Exception {
		String reqTime = nowReqTime();
		String option = (paymentOption == null || paymentOption.isBlank()) ? "abapay_khqr_deeplink" : paymentOption;
		if ("CARD".equalsIgnoreCase(option) || "cards".equalsIgnoreCase(option) || "credit_card".equalsIgnoreCase(option)) {
			option = "cards";
		} else if ("KHQR".equalsIgnoreCase(option) || "abapay".equalsIgnoreCase(option)) {
			option = "abapay_khqr_deeplink";
		}

		String type = "purchase";
		String firstname = "VET";
		String lastname = "Customer";
		String email = "customer@vetcarrental.com";
		String phone = "012345678";
		String currencyStr = (currency != null && !currency.isBlank()) ? currency.toUpperCase() : "USD";
		String amountStr = "KHR".equalsIgnoreCase(currencyStr)
				? amount.setScale(0, java.math.RoundingMode.HALF_UP).toString()
				: amount.setScale(2, java.math.RoundingMode.HALF_UP).toString();
		String returnDeeplink = buildReturnDeeplink("vetexpress://", "vetexpress://");
		String lifetime = String.valueOf(properties.getQrExpiryMinutes());

		String rawData = reqTime
				+ properties.getMerchantId()
				+ tranId
				+ amountStr
				+ firstname
				+ lastname
				+ email
				+ phone
				+ type
				+ option
				+ returnDeeplink
				+ currencyStr
				+ lifetime;

		String hash = hmacSha512(rawData);
		String boundary = "----ABABoundary" + System.currentTimeMillis();
		String body = buildMultipart(boundary,
				part("req_time", reqTime),
				part("merchant_id", properties.getMerchantId()),
				part("tran_id", tranId),
				part("amount", amountStr),
				part("firstname", firstname),
				part("lastname", lastname),
				part("email", email),
				part("phone", phone),
				part("type", type),
				part("payment_option", option),
				part("return_deeplink", returnDeeplink),
				part("currency", currencyStr),
				part("lifetime", lifetime),
				part("hash", hash));

		try {
			String responseBody = post("/api/payment-gateway/v1/payments/purchase", "multipart/form-data; boundary=" + boundary, body);
			log.info("[AbaPaywayService] QR Response for tran_id={}: {}", tranId, responseBody);
			return parseGenerateQrResponse(responseBody);
		} catch (Exception e) {
			log.error("[AbaPaywayService] Failed to generate QR for tran_id={}", tranId, e);
			throw e;
		}
	}

	// * Check transaction status with ABA PayWay
	public CheckTransactionResult checkTransaction(String tranId) throws Exception {
		String reqTime = nowReqTime();
		String rawData = reqTime + properties.getMerchantId() + tranId;
		String hash = hmacSha512(rawData);

		String boundary = "----ABABoundary" + System.currentTimeMillis();
		String body = buildMultipart(boundary,
				part("req_time", reqTime),
				part("merchant_id", properties.getMerchantId()),
				part("tran_id", tranId),
				part("type", "purchase"),
				part("hash", hash));

		String responseBody = post("/api/payment-gateway/v1/payments/check-transaction-2",
				"multipart/form-data; boundary=" + boundary, body);
		return parseCheckResponse(responseBody);
	}

	// * Process pre-auth capture completion
	public boolean capturePreAuth(String transactionId, BigDecimal amount, String currency) {
		try {
			BigDecimal finalAmount = "KHR".equalsIgnoreCase(currency)
					? amount.setScale(0, java.math.RoundingMode.HALF_UP)
					: amount.setScale(2, java.math.RoundingMode.HALF_UP);

			String requestTime = nowReqTime();
			String merchantId = properties.getMerchantId();

			Map<String, Object> authMap = new LinkedHashMap<>();
			authMap.put("mc_id", merchantId);
			authMap.put("tran_id", transactionId);
			authMap.put("complete_amount", finalAmount);

			String merchantAuth = encryptRSA(objectMapper.writeValueAsString(authMap), properties.getPublicKey());
			String hash = hmacSha512(merchantAuth + requestTime + merchantId);

			ObjectNode json = objectMapper.createObjectNode();
			json.put("request_time", requestTime);
			json.put("merchant_id", merchantId);
			json.put("merchant_auth", merchantAuth);
			json.put("hash", hash);

			String responseBody = post("/api/merchant-portal/merchant-access/online-transaction/pre-auth-completion", "application/json", objectMapper.writeValueAsString(json));
			log.info("[AbaPaywayService] Capture Pre-Auth Response: {}", responseBody);

			try {
				JsonNode root = objectMapper.readTree(responseBody);
				String code = root.path("status").path("code").asText(null);
				return "00".equals(code) || "0".equals(code);
			} catch (Exception e) {
				log.error("[AbaPaywayService] Capture Pre-Auth JSON Parsing exception: {}", e.getMessage());
				return false;
			}
		} catch (Exception e) {
			log.error("[AbaPaywayService] Capture Pre-Auth failed", e);
			return false;
		}
	}

	// * Process pre-auth cancellation
	public boolean cancelPreAuth(String transactionId) {
		try {
			String requestTime = nowReqTime();
			String merchantId = properties.getMerchantId();

			Map<String, Object> authMap = new LinkedHashMap<>();
			authMap.put("mc_id", merchantId);
			authMap.put("tran_id", transactionId);

			String merchantAuth = encryptRSA(objectMapper.writeValueAsString(authMap), properties.getPublicKey());
			String hash = hmacSha512(merchantId + merchantAuth + requestTime);

			ObjectNode json = objectMapper.createObjectNode();
			json.put("request_time", requestTime);
			json.put("merchant_id", merchantId);
			json.put("merchant_auth", merchantAuth);
			json.put("hash", hash);

			String responseBody = post("/api/merchant-portal/merchant-access/online-transaction/pre-auth-cancellation", "application/json", objectMapper.writeValueAsString(json));
			log.info("[AbaPaywayService] Cancel Pre-Auth Response: {}", responseBody);

			try {
				JsonNode root = objectMapper.readTree(responseBody);
				String code = root.path("status").path("code").asText(null);
				return "00".equals(code) || "0".equals(code);
			} catch (Exception e) {
				log.error("[AbaPaywayService] Cancel Pre-Auth JSON Parsing exception: {}", e.getMessage());
				return false;
			}
		} catch (Exception e) {
			log.error("[AbaPaywayService] Cancel Pre-Auth failed", e);
			return false;
		}
	}

	// * Helper: hmacSha512 hash calculation
	private String hmacSha512(String data) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA512");
		mac.init(new SecretKeySpec(properties.getApiKey().getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
		return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
	}

	// * Helper: encrypt payload via RSA with 117-byte chunking
	private String encryptRSA(String data, String keyStr) throws Exception {
		if (keyStr == null || keyStr.isBlank()) throw new IllegalArgumentException("ABA Public Key is missing!");
		String cleanKey = keyStr.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "").replaceAll("\\s", "");
		PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(cleanKey)));
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);

		byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
		java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
		int maxChunk = 117;
		for (int offset = 0; offset < dataBytes.length; offset += maxChunk) {
			int len = Math.min(maxChunk, dataBytes.length - offset);
			outputStream.write(cipher.doFinal(dataBytes, offset, len));
		}
		return Base64.getEncoder().encodeToString(outputStream.toByteArray());
	}

	private static String part(String name, String value) {
		return "Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n" + (value == null ? "" : value);
	}

	private static String buildMultipart(String boundary, String... parts) {
		StringBuilder sb = new StringBuilder();
		for (String part : parts) {
			sb.append("--").append(boundary).append("\r\n");
			sb.append(part).append("\r\n");
		}
		sb.append("--").append(boundary).append("--");
		return sb.toString();
	}

	private String post(String path, String contentType, String body) throws Exception {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(getApiBaseUrl() + path))
				.header("Content-Type", contentType)
				.header("Accept", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
				.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new IOException("ABA PayWay returned HTTP " + response.statusCode() + ": " + response.body());
		}
		return response.body();
	}

	private String getApiBaseUrl() {
		if ("prod".equalsIgnoreCase(activeProfile)) {
			return "https://checkout.payway.com.kh";
		}
		return "https://checkout-sandbox.payway.com.kh";
	}

	private String nowReqTime() {
		return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
	}

	PaymentResponse parseGenerateQrResponse(String body) {
		try {
			JsonNode root = objectMapper.readTree(body);
			JsonNode statusNode = root.path("status");
			String code = textVal(statusNode, "code");
			String message = textVal(statusNode, "message");
			String tranId = textVal(statusNode, "tran_id");

			if (!"0".equals(code) && !"00".equals(code)) {
				throw new RuntimeException("ABA PayWay returned error code " + code + ": " + message);
			}

			return PaymentResponse.builder()
					.transactionId(tranId)
					.qrString(textVal(root, "qr_string") != null ? textVal(root, "qr_string") : textVal(root, "qrString"))
					.qrImage(textVal(root, "qr_image") != null ? textVal(root, "qr_image") : textVal(root, "qrImage"))
					.abapayUrl(textVal(root, "abapay_deeplink") != null ? textVal(root, "abapay_deeplink") : textVal(root, "abapayUrl"))
					.checkoutQrUrl(textVal(root, "checkout_qr_url"))
					.appStore(textVal(root, "app_store"))
					.playStore(textVal(root, "play_store"))
					.paymentStatus(1)
					.paymentStatusLabel("PENDING")
					.message(message != null ? message : "Payment QR generated successfully")
					.build();
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse generate-qr response: " + e.getMessage(), e);
		}
	}

	private CheckTransactionResult parseCheckResponse(String body) {
		try {
			JsonNode root = objectMapper.readTree(body);
			JsonNode data = root.path("data");
			String payStatus = textVal(data, "payment_status");
			Integer code = intVal(data, "payment_status_code");
			String apv = textVal(data, "apv");
			Double total = doubleVal(data, "total_amount");
			return new CheckTransactionResult(payStatus, code, apv, total);
		} catch (Exception e) {
			return new CheckTransactionResult(null, null, null, null);
		}
	}

	private String textVal(JsonNode node, String field) {
		JsonNode f = node.path(field);
		return f.isMissingNode() || f.isNull() ? null : f.asText();
	}

	private Integer intVal(JsonNode node, String field) {
		JsonNode f = node.path(field);
		return f.isMissingNode() || f.isNull() ? null : f.asInt();
	}

	private Double doubleVal(JsonNode node, String field) {
		JsonNode f = node.path(field);
		return f.isMissingNode() || f.isNull() ? null : f.asDouble();
	}

	String buildReturnDeeplink(String iosScheme, String androidScheme) {
		if (iosScheme == null || iosScheme.isEmpty()) {
			throw new IllegalArgumentException("iosScheme is required for return_deeplink");
		}
		if (androidScheme == null || androidScheme.isEmpty()) {
			throw new IllegalArgumentException("androidScheme is required for return_deeplink");
		}
		String safeIosScheme = iosScheme.replace("\\", "\\\\").replace("\"", "\\\"");
		String safeAndroidScheme = androidScheme.replace("\\", "\\\\").replace("\"", "\\\"");
		String json = "{\"ios_scheme\":\"" + safeIosScheme + "\",\"android_scheme\":\"" + safeAndroidScheme + "\"}";
		return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
	}
}
