package com.Car_Rental_API.module.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

// * Configuration properties for ABA PayWay API
@Data
@Configuration
@ConfigurationProperties(prefix = "aba")
public class AbaProperties {
	private String apiKey = "";
	private String merchantId = "";
	private String publicKey = "";
	private int qrExpiryMinutes = 5;
}
