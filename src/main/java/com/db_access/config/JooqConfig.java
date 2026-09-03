package com.db_access.config;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class JooqConfig {

    @Bean
    public DSLContext dslContext(DataSource dataSource) {
        Settings settings = new Settings()
                .withRenderCatalog(false)
                .withRenderSchema(false);

        DefaultConfiguration configuration = new DefaultConfiguration();
        configuration.set(dataSource);
        configuration.set(SQLDialect.MYSQL);
        configuration.set(settings);

        return DSL.using(configuration);
    }
}
