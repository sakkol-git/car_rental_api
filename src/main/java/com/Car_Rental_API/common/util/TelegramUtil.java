package com.Car_Rental_API.common.util;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TelegramUtil {

    private static final String TELEGRAM_API_BASE = "https://api.telegram.org";
    private static final int TELEGRAM_MAX_MESSAGE_LENGTH = 4096;
    private static final int MESSAGE_TRUNCATE_LIMIT = 2000;
    private static final int MAX_STACK_LINES = 20;
    private static final int MAX_FRAMES_PER_CAUSE = 6;

    private final RestTemplate restTemplate;

    private final String botToken;
    private final String chatId;
    private final String appDescription;
    private final String defaultEnv;
    private final String basePackage;

    public TelegramUtil(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${telegram.bot-token}") String botToken,
            @Value("${telegram.chat-id}") String chatId,
            @Value("${spring.application.name}") String appDescription,
            @Value("${spring.profiles.active:dev}") String defaultEnv,
            @Value("${app.base-package:}") String basePackage) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
        this.botToken = botToken;
        this.chatId = chatId;
        this.appDescription = appDescription;
        this.defaultEnv = defaultEnv;
        this.basePackage = basePackage;
    }

    @Async
    @CircuitBreaker(name = "defaultService", fallbackMethod = "sendErrorNotificationFallback")
    public void sendErrorNotification(ProblemDetail error) {
        if (error == null) {
            log.warn("sendErrorNotification called with null error payload; skipping.");
            return;
        }
        if (!isConfigured()) {
            log.warn("Telegram botToken or chatId is missing. Cannot send notification.");
            return;
        }
        post(buildErrorMessage(error));
    }

    @SuppressWarnings("unused") // invoked reflectively by resilience4j
    private void sendErrorNotificationFallback(ProblemDetail error, Throwable ex) {
        log.error("Telegram error notification failed (fallback): {}", ex.getMessage(), ex);
    }

    @Async
    @CircuitBreaker(name = "defaultService", fallbackMethod = "sendNotificationFallback")
    public void sendNotification(String title, String message) {
        sendNotification(title, message, defaultEnv);
    }

    @SuppressWarnings("unused")
    private void sendNotificationFallback(String title, String message, Throwable ex) {
        log.error("Telegram notification failed (fallback): {}", ex.getMessage(), ex);
    }

    @Async
    @CircuitBreaker(name = "defaultService", fallbackMethod = "sendNotificationWithModeFallback")
    public void sendNotification(String title, String message, String mode) {
        if (!isConfigured()) {
            log.warn("Telegram botToken or chatId is missing. Cannot send notification.");
            return;
        }
        String targetMode = (mode != null && !mode.isBlank()) ? mode : defaultEnv;
        String text = "<b>🚨 " + escapeHtml(appDescription) + "</b>\n"
                + "-------------------------------------\n"
                + "<b>• Title:</b> <code>" + escapeHtml(title) + "</code>\n"
                + "<b>• Mode:</b> <code>" + escapeHtml(targetMode.toUpperCase()) + "</code>\n"
                + "<b>• Message:</b> " + escapeHtml(truncate(message)) + "\n";
        post(text);
    }

    @SuppressWarnings("unused")
    private void sendNotificationWithModeFallback(String title, String message, String mode, Throwable ex) {
        log.error("Telegram notification failed (fallback): {}", ex.getMessage(), ex);
    }

    // Shared HTTP post to Telegram API using HTML parse mode
    private void post(String text) {
        String safeText = text.length() > TELEGRAM_MAX_MESSAGE_LENGTH
                ? text.substring(0, TELEGRAM_MAX_MESSAGE_LENGTH - 3) + "..."
                : text;

        Map<String, Object> body = Map.of(
                "chat_id", chatId,
                "text", safeText,
                "parse_mode", "HTML");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = UriComponentsBuilder.fromHttpUrl(TELEGRAM_API_BASE)
                .path("/bot{token}/sendMessage")
                .buildAndExpand(botToken)
                .toUriString();

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), Map.class);
            Map<?, ?> respBody = response.getBody();
            if (respBody != null && Boolean.FALSE.equals(respBody.get("ok"))) {
                // Never log the url here — it contains the bot token.
                log.warn("Telegram API rejected message: {}", respBody.get("description"));
            }
        } catch (RestClientException ex) {
            // Rethrow so @CircuitBreaker's fallback handles/logs it; don't log the url (contains token).
            throw ex;
        }
    }

    /**
     * Builds the Telegram error message from a plain {@link ProblemDetail}.
     * Custom fields (mode, endpoint, method, username, stackTrace) are read
     * from ProblemDetail's RFC-7807 extension-properties map, e.g.:
     * <pre>
     *   ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
     *   pd.setDetail(ex.getMessage());
     *   pd.setProperty("mode", activeProfile);
     *   pd.setProperty("endpoint", request.getRequestURI());
     *   pd.setProperty("method", request.getMethod());
     *   pd.setProperty("username", principal.getName());
     *   pd.setProperty("stackTrace", ExceptionUtils.getStackTrace(ex));
     * </pre>
     */
    private String buildErrorMessage(ProblemDetail error) {
        Map<String, Object> props = Optional.ofNullable(error.getProperties()).orElse(Map.of());

        String mode = stringProp(props, "mode");
        String targetMode = (mode != null && !mode.isBlank()) ? mode : defaultEnv;

        String endpoint = stringProp(props, "endpoint");
        String method = stringProp(props, "method");
        String username = stringProp(props, "username");
        String stackTrace = stringProp(props, "stackTrace");

        String detail = error.getDetail() != null ? error.getDetail() : error.getTitle();

        StringBuilder base = new StringBuilder()
                .append("<b>🚨 ").append(escapeHtml(appDescription)).append("</b>\n")
                .append("-------------------------------------\n")
                .append("<b>• Mode:</b> <code>").append(escapeHtml(targetMode.toUpperCase())).append("</code>\n")
                .append("<b>• Message:</b> ").append(escapeHtml(truncate(detail))).append("\n");

        if (endpoint != null) {
            base.append("<b>• Endpoint:</b> <code>")
                    .append(escapeHtml((method != null ? method + " " : "") + endpoint))
                    .append("</code>\n");
        }
        if (username != null) {
            base.append("<b>• User:</b> ").append(escapeHtml(username)).append("\n");
        }

        if (stackTrace != null && !stackTrace.isBlank()) {
            String trace = filterStackTrace(stackTrace);
            int maxTrace = Math.max(0, TELEGRAM_MAX_MESSAGE_LENGTH - base.length() - 100); // reserve tag overhead
            if (trace.length() > maxTrace) {
                trace = trace.substring(0, maxTrace) + "...";
            }
            base.append("<b>• Stack Trace:</b>\n<blockquote expandable><pre><code>")
                    .append(escapeHtml(trace))
                    .append("</code></pre></blockquote>");
        }

        return base.toString();
    }

    private String stringProp(Map<String, Object> props, String key) {
        Object val = props.get(key);
        return val != null ? val.toString() : null;
    }

    // Filter stack trace to keep application-specific frames and causes
    private String filterStackTrace(String trace) {
        if (trace == null || trace.isBlank()) {
            return "";
        }
        String[] lines = trace.split("\r?\n");
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String line : lines) {
            boolean isCauseOrAppFrame = count == 0
                    || line.contains("Caused by:")
                    || (!basePackage.isBlank() && line.toLowerCase().contains(basePackage.toLowerCase()));
            if (isCauseOrAppFrame) {
                sb.append(line).append("\n");
                count++;
            } else if (count < MAX_FRAMES_PER_CAUSE && line.trim().startsWith("at ")) {
                sb.append(line).append("\n");
                count++;
            }
            if (count >= MAX_STACK_LINES) {
                break;
            }
        }
        return !sb.isEmpty() ? sb.toString().trim() : trace;
    }

    private boolean isConfigured() {
        return botToken != null && !botToken.isBlank() && chatId != null && !chatId.isBlank();
    }

    private String truncate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() > MESSAGE_TRUNCATE_LIMIT
                ? text.substring(0, MESSAGE_TRUNCATE_LIMIT) + "..."
                : text;
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}