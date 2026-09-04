package com.Car_Rental_API.module.report.customer_review.repository;

import com.db_access.jooq.tables.records.CustomerReviewsRecord;
import com.Car_Rental_API.module.report.customer_review.model.CustomerReview;
import com.Car_Rental_API.module.report.customer_review.dto.request.CustomerReviewFilterRequest;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.db_access.jooq.tables.CustomerReviews.CUSTOMER_REVIEWS;
import static com.db_access.jooq.tables.Customers.CUSTOMERS;
import static com.db_access.jooq.tables.SalesOrders.SALES_ORDERS;
import static com.db_access.jooq.tables.Vehicles.VEHICLES;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.concat;
import static org.jooq.impl.DSL.val;

@Repository
@RequiredArgsConstructor
public class CustomerReviewRepository {

    private final DSLContext dsl;

    private Condition buildCondition(CustomerReviewFilterRequest req) {
        Condition cond = CUSTOMER_REVIEWS.IS_ACTIVE.eq((byte) 1);
        if (req == null) return cond;

        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            String like = "%" + req.getKeyword().trim() + "%";
            cond = cond.and(CUSTOMERS.FULL_NAME.likeIgnoreCase(like)
                    .or(CUSTOMERS.PHONE_NUMBER.likeIgnoreCase(like))
                    .or(SALES_ORDERS.ORDER_NO.likeIgnoreCase(like))
                    .or(VEHICLES.NAME_EN.likeIgnoreCase(like)));
        }
        if (req.getCustomerId() != null)   cond = cond.and(CUSTOMER_REVIEWS.CUSTOMER_ID.eq(req.getCustomerId()));
        if (req.getVehicleId() != null)    cond = cond.and(CUSTOMER_REVIEWS.VEHICLE_ID.eq(req.getVehicleId()));
        if (req.getSalesOrderId() != null) cond = cond.and(CUSTOMER_REVIEWS.SALES_ORDER_ID.eq(req.getSalesOrderId()));
        if (req.getDateFrom() != null)     cond = cond.and(CUSTOMER_REVIEWS.CREATED.greaterOrEqual(java.time.LocalDate.parse(req.getDateFrom()).atStartOfDay()));
        if (req.getDateTo() != null)       cond = cond.and(CUSTOMER_REVIEWS.CREATED.lessOrEqual(java.time.LocalDate.parse(req.getDateTo()).atTime(23, 59, 59)));

        return cond;
    }

    // * Paged list with joined customer, vehicle and order display fields
    public List<CustomerReview> findAll(CustomerReviewFilterRequest req) {
        int limit  = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        return dsl.select(
                CUSTOMER_REVIEWS.asterisk(),
                coalesce(CUSTOMERS.FULL_NAME, SALES_ORDERS.PASSENGER_NAME, concat(val("Customer #"), CUSTOMER_REVIEWS.CUSTOMER_ID)).as("customerName"),
                coalesce(CUSTOMERS.PHONE_NUMBER, SALES_ORDERS.PASSENGER_PHONE, val("")).as("customerPhone"),
                SALES_ORDERS.ORDER_NO.as("orderNo"),
                VEHICLES.NAME_EN.as("vehicleName")
        )
        .from(CUSTOMER_REVIEWS)
        .leftJoin(CUSTOMERS).on(CUSTOMERS.ID.eq(CUSTOMER_REVIEWS.CUSTOMER_ID))
        .leftJoin(SALES_ORDERS).on(SALES_ORDERS.ID.eq(CUSTOMER_REVIEWS.SALES_ORDER_ID))
        .leftJoin(VEHICLES).on(VEHICLES.ID.eq(CUSTOMER_REVIEWS.VEHICLE_ID))
        .where(buildCondition(req))
        .orderBy(CUSTOMER_REVIEWS.ID.desc())
        .limit(limit).offset(offset)
        .fetchInto(CustomerReview.class);
    }

    public long countAll(CustomerReviewFilterRequest req) {
        return dsl.selectCount()
                .from(CUSTOMER_REVIEWS)
                .leftJoin(CUSTOMERS).on(CUSTOMERS.ID.eq(CUSTOMER_REVIEWS.CUSTOMER_ID))
                .leftJoin(SALES_ORDERS).on(SALES_ORDERS.ID.eq(CUSTOMER_REVIEWS.SALES_ORDER_ID))
                .leftJoin(VEHICLES).on(VEHICLES.ID.eq(CUSTOMER_REVIEWS.VEHICLE_ID))
                .where(buildCondition(req))
                .fetchOne(0, long.class);
    }

    public Optional<CustomerReview> findById(Long id) {
        return dsl.select(
                CUSTOMER_REVIEWS.asterisk(),
                coalesce(CUSTOMERS.FULL_NAME, SALES_ORDERS.PASSENGER_NAME, concat(val("Customer #"), CUSTOMER_REVIEWS.CUSTOMER_ID)).as("customerName"),
                coalesce(CUSTOMERS.PHONE_NUMBER, SALES_ORDERS.PASSENGER_PHONE, val("")).as("customerPhone"),
                SALES_ORDERS.ORDER_NO.as("orderNo"),
                VEHICLES.NAME_EN.as("vehicleName")
        )
        .from(CUSTOMER_REVIEWS)
        .leftJoin(CUSTOMERS).on(CUSTOMERS.ID.eq(CUSTOMER_REVIEWS.CUSTOMER_ID))
        .leftJoin(SALES_ORDERS).on(SALES_ORDERS.ID.eq(CUSTOMER_REVIEWS.SALES_ORDER_ID))
        .leftJoin(VEHICLES).on(VEHICLES.ID.eq(CUSTOMER_REVIEWS.VEHICLE_ID))
        .where(CUSTOMER_REVIEWS.ID.eq(id).and(CUSTOMER_REVIEWS.IS_ACTIVE.eq((byte) 1)))
        .fetchOptionalInto(CustomerReview.class);
    }

    // * Insert new review submitted from mobile app
    public CustomerReview save(CustomerReview review) {
        CustomerReviewsRecord rec = dsl.newRecord(CUSTOMER_REVIEWS);
        rec.from(review);
        rec.setId(null);
        rec.setIsDisabled((byte) 0);
        rec.setIsActive((byte) 1);
        rec.setCreated(LocalDateTime.now());
        dsl.insertInto(CUSTOMER_REVIEWS).set(rec).execute();
        review.setId(dsl.lastID().longValue());
        return review;
    }

    // * Toggle visibility: 0 = enabled, 1 = disabled from app display
    public void updateDisabled(Long id, Byte isDisabled) {
        dsl.update(CUSTOMER_REVIEWS)
                .set(CUSTOMER_REVIEWS.IS_DISABLED, isDisabled)
                .set(CUSTOMER_REVIEWS.MODIFIED, LocalDateTime.now())
                .where(CUSTOMER_REVIEWS.ID.eq(id))
                .execute();
    }

    public void deleteById(Long id) {
        dsl.update(CUSTOMER_REVIEWS)
                .set(CUSTOMER_REVIEWS.IS_ACTIVE, (byte) 0)
                .set(CUSTOMER_REVIEWS.MODIFIED, LocalDateTime.now())
                .where(CUSTOMER_REVIEWS.ID.eq(id))
                .execute();
    }
}
