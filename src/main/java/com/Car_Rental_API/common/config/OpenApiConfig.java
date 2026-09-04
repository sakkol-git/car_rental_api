package com.Car_Rental_API.common.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(name = "BearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {

	@Bean
	public OpenAPI customOpenAPI(
			@Value("${spring.application.name}") String appName,
			@Value("${spring.application.version}") String appVersion) {

		return new OpenAPI()
				.info(new Info().title(appName).version(appVersion))
				.addServersItem(new Server().url("/").description("Default Server"))
				.addSecurityItem(new SecurityRequirement().addList("BearerAuth"));
	}

	@Bean
	public GroupedOpenApi publicApi() {
		return GroupedOpenApi.builder().group("0.ALL APIs").pathsToMatch("/**").build();
	}
}
