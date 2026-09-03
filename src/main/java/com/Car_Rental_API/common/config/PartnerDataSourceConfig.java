package com.Car_Rental_API.common.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class PartnerDataSourceConfig {

    @Value("${partner.datasource.url:}")
    private String url;

    @Value("${partner.datasource.username:}")
    private String username;

    @Value("${partner.datasource.password:}")
    private String password;

    @Value("${partner.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String driverClassName;

    @Value("${partner.datasource.hikari.minimum-idle:1}")
    private int minimumIdle;

    @Value("${partner.datasource.hikari.maximum-pool-size:3}")
    private int maximumPoolSize;

    @Value("${partner.datasource.hikari.connection-timeout:5000}")
    private long connectionTimeout;

    @Value("${partner.datasource.hikari.idle-timeout:30000}")
    private long idleTimeout;

    @Value("${partner.datasource.hikari.max-lifetime:120000}")
    private long maxLifetime;

    // * Initialize secondary DataSource for Partner System DB
    @Bean(name = "partnerDataSource", destroyMethod = "close")
    @ConditionalOnProperty(name = "partner.datasource.url", matchIfMissing = false)
    public DataSource partnerDataSource() {
        if (url == null || url.isBlank()) {
            log.info("Partner datasource URL not configured — cross-platform auth disabled");
            return null;
        }

        HikariConfig config = new HikariConfig();
        config.setPoolName("HikariPool-Partner");
        config.setDriverClassName(driverClassName);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMinimumIdle(minimumIdle);
        config.setMaximumPoolSize(maximumPoolSize);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setConnectionTestQuery("SELECT 1");

        log.info("Partner secondary datasource initialized: {}", url.replaceAll("//[^@]+@", "//<credentials>@"));
        return new HikariDataSource(config);
    }
}


