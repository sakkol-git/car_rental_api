package com.db_access.jooq;

import com.Car_Rental_API.CarRentalApiApplication;
import com.db_access.config.JooqConfig;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = CarRentalApiApplication.class, properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3307/1137_VET_Car_RP_Database",
        "spring.datasource.username=root",
        "spring.datasource.password=root",
        "spring.flyway.enabled=false"
})
@Import(JooqConfig.class)
public class JooqIntegrationTest {

    @Autowired
    private DSLContext dsl;

    @Test
    public void testJooqQuery() {
        assertNotNull(dsl, "DSLContext should be injected");
        assertDoesNotThrow(() -> {
            Integer count = dsl.selectCount().from(com.db_access.jooq.Tables.VEHICLES).fetchOne(0, int.class);
            System.out.println("Vehicles count: " + count);
        });
    }
}
