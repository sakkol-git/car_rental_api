package com.Car_Rental_API.module.report.sale_order_report.repository;

import com.Car_Rental_API.module.report.sale_order_report.dto.request.SaleOrderReportFilterRequest;
import com.Car_Rental_API.module.report.sale_order_report.dto.response.SaleOrderReportRow;
import com.Car_Rental_API.module.report.sale_order_report.dto.response.SaleOrderReportSummary;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.db_access.jooq.tables.Customers.CUSTOMERS;
import static com.db_access.jooq.tables.SalesOrders.SALES_ORDERS;
import static com.db_access.jooq.tables.VehicleCategories.VEHICLE_CATEGORIES;
import static com.db_access.jooq.tables.VehicleRentalTypes.VEHICLE_RENTAL_TYPES;
import static com.db_access.jooq.tables.Vehicles.VEHICLES;

@Repository
@RequiredArgsConstructor
public class SaleOrderReportRepository {

    private final DSLContext dsl;

    // * Build dynamic filter conditions for report queries
    private Condition buildCondition(SaleOrderReportFilterRequest req) {
        Condition cond = SALES_ORDERS.IS_ACTIVE.eq((byte) 1);
        if (req == null) return cond;

        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            String like = "%" + req.getKeyword().trim() + "%";
            cond = cond.and(
                SALES_ORDERS.ORDER_NO.likeIgnoreCase(like)
                    .or(SALES_ORDERS.PASSENGER_NAME.likeIgnoreCase(like))
                    .or(SALES_ORDERS.PASSENGER_PHONE.likeIgnoreCase(like))
                    .or(CUSTOMERS.FULL_NAME.likeIgnoreCase(like))
                    .or(CUSTOMERS.PHONE_NUMBER.likeIgnoreCase(like))
                    .or(VEHICLES.NAME_EN.likeIgnoreCase(like))
                    .or(SALES_ORDERS.PICKUP_ADDRESS.likeIgnoreCase(like))
                    .or(SALES_ORDERS.DROPOFF_ADDRESS.likeIgnoreCase(like))
                    .or(SALES_ORDERS.REMARK.likeIgnoreCase(like))
            );
        }
        if (req.getOrderType() != null)           cond = cond.and(SALES_ORDERS.ORDER_TYPE.eq(req.getOrderType()));
        if (req.getOrderStatus() != null)         cond = cond.and(SALES_ORDERS.ORDER_STATUS.eq(req.getOrderStatus()));
        if (req.getPaymentStatus() != null)       cond = cond.and(SALES_ORDERS.PAYMENT_STATUS.eq(req.getPaymentStatus()));
        if (req.getVehicleCategoryId() != null)    cond = cond.and(SALES_ORDERS.VEHICLE_CATEGORY_ID.eq(req.getVehicleCategoryId()));
        if (req.getVehicleRentalTypeId() != null) cond = cond.and(SALES_ORDERS.VEHICLE_RENTAL_TYPE_ID.eq(req.getVehicleRentalTypeId()));
        if (req.getJourneyType() != null)         cond = cond.and(SALES_ORDERS.JOURNEY_TYPE.eq(req.getJourneyType()));
        if (req.getPaymentType() != null)         cond = cond.and(SALES_ORDERS.PAYMENT_TYPE.eq(req.getPaymentType()));
        if (req.getPaymentMethod() != null)       cond = cond.and(SALES_ORDERS.PAYMENT_METHOD.eq(req.getPaymentMethod().byteValue()));
        if (req.getPickupLocationId() != null)    cond = cond.and(SALES_ORDERS.PICKUP_LOCATION_ID.eq(req.getPickupLocationId()));
        if (req.getDropoffLocationId() != null)   cond = cond.and(SALES_ORDERS.DROPOFF_LOCATION_ID.eq(req.getDropoffLocationId()));

        if (req.getCustomerId() != null && req.getCreatedBy() != null) {
            cond = cond.and(
                SALES_ORDERS.CUSTOMER_ID.eq(req.getCustomerId())
                    .or(SALES_ORDERS.CREATED_BY.eq(req.getCreatedBy()))
            );
        } else if (req.getCustomerId() != null) {
            cond = cond.and(SALES_ORDERS.CUSTOMER_ID.eq(req.getCustomerId()));
        } else if (req.getCreatedBy() != null) {
            cond = cond.and(SALES_ORDERS.CREATED_BY.eq(req.getCreatedBy()));
        }
        if (req.getVehicleId() != null)           cond = cond.and(SALES_ORDERS.VEHICLE_ID.eq(req.getVehicleId()));
        if (req.getDateFrom() != null && !req.getDateFrom().isBlank()) {
            cond = cond.and(SALES_ORDERS.START_DATE.greaterOrEqual(LocalDate.parse(req.getDateFrom().trim())));
        }
        if (req.getDateTo() != null && !req.getDateTo().isBlank()) {
            cond = cond.and(SALES_ORDERS.END_DATE.lessOrEqual(LocalDate.parse(req.getDateTo().trim())));
        }
        if (req.getCreatedFrom() != null && !req.getCreatedFrom().isBlank()) {
            cond = cond.and(SALES_ORDERS.CREATED.greaterOrEqual(LocalDate.parse(req.getCreatedFrom().trim()).atStartOfDay()));
        }
        if (req.getCreatedTo() != null && !req.getCreatedTo().isBlank()) {
            cond = cond.and(SALES_ORDERS.CREATED.lessOrEqual(LocalDate.parse(req.getCreatedTo().trim()).atTime(23, 59, 59)));
        }

        return cond;
    }

    // * Define select fields for report with table asterisk and joined descriptions
    private List<SelectFieldOrAsterisk> reportFields() {
        List<SelectFieldOrAsterisk> fields = new ArrayList<>(Arrays.asList(SALES_ORDERS.fields()));
        fields.add(CUSTOMERS.FULL_NAME.as("customerName"));
        fields.add(CUSTOMERS.PHONE_NUMBER.as("customerPhone"));
        fields.add(VEHICLES.NAME_EN.as("vehicleName"));
        fields.add(VEHICLE_CATEGORIES.NAME_KH.as("vehicleCategoryNameKh"));
        fields.add(VEHICLE_CATEGORIES.NAME_EN.as("vehicleCategoryNameEn"));
        fields.add(VEHICLE_CATEGORIES.NAME_ZH.as("vehicleCategoryNameZh"));
        fields.add(VEHICLE_RENTAL_TYPES.NAME_EN.as("vehicleRentalTypeName"));
        fields.add(SALES_ORDERS.PICKUP_ADDRESS.as("pickupLocationName"));
        fields.add(SALES_ORDERS.DROPOFF_ADDRESS.as("dropoffLocationName"));
        return fields;
    }

    // * Full report query with all relevant joins
    public List<SaleOrderReportRow> findAll(SaleOrderReportFilterRequest req) {
        int limit  = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        return dsl.select(reportFields())
                .from(SALES_ORDERS)
                .leftJoin(CUSTOMERS).on(CUSTOMERS.ID.eq(SALES_ORDERS.CUSTOMER_ID))
                .leftJoin(VEHICLES).on(VEHICLES.ID.eq(SALES_ORDERS.VEHICLE_ID))
                .leftJoin(VEHICLE_CATEGORIES).on(VEHICLE_CATEGORIES.ID.eq(SALES_ORDERS.VEHICLE_CATEGORY_ID))
                .leftJoin(VEHICLE_RENTAL_TYPES).on(VEHICLE_RENTAL_TYPES.ID.eq(SALES_ORDERS.VEHICLE_RENTAL_TYPE_ID))
                .where(buildCondition(req))
                .orderBy(
                    DSL.coalesce(SALES_ORDERS.PAYMENT_STATUS, (byte) 1).asc(),
                    SALES_ORDERS.ID.desc()
                )
                .limit(limit).offset(offset)
                .fetchInto(SaleOrderReportRow.class);
    }

    // * Count matching orders for pagination
    public long countAll(SaleOrderReportFilterRequest req) {
        return dsl.selectCount()
                .from(SALES_ORDERS)
                .leftJoin(CUSTOMERS).on(CUSTOMERS.ID.eq(SALES_ORDERS.CUSTOMER_ID))
                .leftJoin(VEHICLES).on(VEHICLES.ID.eq(SALES_ORDERS.VEHICLE_ID))
                .leftJoin(VEHICLE_RENTAL_TYPES).on(VEHICLE_RENTAL_TYPES.ID.eq(SALES_ORDERS.VEHICLE_RENTAL_TYPE_ID))
                .where(buildCondition(req))
                .fetchOne(0, long.class);
    }

    // * Calculate report summary: status totals, grouped deposit & paid metrics, financial aggregates
    public SaleOrderReportSummary getSummary(SaleOrderReportFilterRequest req) {
        Condition cond = buildCondition(req);

        var rec = dsl.select(
                DSL.count().as("totalOrders"),
                DSL.count(DSL.when(SALES_ORDERS.ORDER_STATUS.eq((byte) 1), 1)).as("toDoCount"),
                DSL.count(DSL.when(SALES_ORDERS.ORDER_STATUS.eq((byte) 2), 1)).as("inProgressCount"),
                DSL.count(DSL.when(SALES_ORDERS.ORDER_STATUS.eq((byte) 3), 1)).as("completeCount"),
                DSL.count(DSL.when(SALES_ORDERS.ORDER_STATUS.eq((byte) 4), 1)).as("voidCount"),
                DSL.count(DSL.when(SALES_ORDERS.ORDER_STATUS.eq((byte) 5), 1)).as("rejectedCount"),

                // * Payment Status Counts (1: Booking, 2: Deposit, 3: Paid, 4: Expired / Cancelled)
                DSL.count(DSL.when(SALES_ORDERS.PAYMENT_STATUS.eq((byte) 1), 1)).as("bookingCount"),
                DSL.count(DSL.when(SALES_ORDERS.PAYMENT_STATUS.eq((byte) 2), 1)).as("depositCount"),
                DSL.count(DSL.when(SALES_ORDERS.PAYMENT_STATUS.eq((byte) 3), 1)).as("paidCount"),
                DSL.count(DSL.when(SALES_ORDERS.PAYMENT_STATUS.eq((byte) 4), 1)).as("expiredCount"),

                // * Grouped financials
                DSL.coalesce(DSL.sum(DSL.when(SALES_ORDERS.PAYMENT_STATUS.eq((byte) 2), SALES_ORDERS.PAID_AMOUNT)), BigDecimal.ZERO).as("depositPaidAmount"),
                DSL.coalesce(DSL.sum(DSL.when(SALES_ORDERS.PAYMENT_STATUS.eq((byte) 3), SALES_ORDERS.PAID_AMOUNT)), BigDecimal.ZERO).as("fullyPaidAmount"),
                DSL.coalesce(DSL.sum(DSL.when(SALES_ORDERS.PAYMENT_STATUS.eq((byte) 1), SALES_ORDERS.TOTAL_AMOUNT)), BigDecimal.ZERO).as("bookingTotalAmount"),

                // * Overall Totals
                DSL.coalesce(DSL.sum(SALES_ORDERS.TOTAL_AMOUNT), BigDecimal.ZERO).as("totalAmount"),
                DSL.coalesce(DSL.sum(SALES_ORDERS.PAID_AMOUNT), BigDecimal.ZERO).as("totalPaid"),
                DSL.coalesce(DSL.sum(SALES_ORDERS.REMAINING_AMOUNT), BigDecimal.ZERO).as("totalRemaining"),
                DSL.coalesce(DSL.sum(SALES_ORDERS.DISCOUNT_AMOUNT), BigDecimal.ZERO).as("totalDiscount")
        )
        .from(SALES_ORDERS)
        .leftJoin(CUSTOMERS).on(CUSTOMERS.ID.eq(SALES_ORDERS.CUSTOMER_ID))
        .leftJoin(VEHICLES).on(VEHICLES.ID.eq(SALES_ORDERS.VEHICLE_ID))
        .leftJoin(VEHICLE_RENTAL_TYPES).on(VEHICLE_RENTAL_TYPES.ID.eq(SALES_ORDERS.VEHICLE_RENTAL_TYPE_ID))
        .where(cond)
        .fetchOne();

        if (rec == null) {
            return emptySummary();
        }

        return SaleOrderReportSummary.builder()
                .totalOrders(getLong(rec, "totalOrders"))
                .toDoCount(getLong(rec, "toDoCount"))
                .inProgressCount(getLong(rec, "inProgressCount"))
                .completeCount(getLong(rec, "completeCount"))
                .voidCount(getLong(rec, "voidCount"))
                .rejectedCount(getLong(rec, "rejectedCount"))
                .bookingCount(getLong(rec, "bookingCount"))
                .depositCount(getLong(rec, "depositCount"))
                .paidCount(getLong(rec, "paidCount"))
                .expiredCount(getLong(rec, "expiredCount"))
                .totalDepositAmount(getDecimal(rec, "depositPaidAmount"))
                .totalFullyPaidAmount(getDecimal(rec, "fullyPaidAmount"))
                .totalBookingAmount(getDecimal(rec, "bookingTotalAmount"))
                .totalAmount(getDecimal(rec, "totalAmount"))
                .totalPaid(getDecimal(rec, "totalPaid"))
                .totalRemaining(getDecimal(rec, "totalRemaining"))
                .totalDiscount(getDecimal(rec, "totalDiscount"))
                .build();
    }

    private long getLong(org.jooq.Record rec, String field) {
        Long val = rec.get(field, Long.class);
        return val != null ? val : 0L;
    }

    private BigDecimal getDecimal(org.jooq.Record rec, String field) {
        BigDecimal val = rec.get(field, BigDecimal.class);
        return val != null ? val : BigDecimal.ZERO;
    }

    private SaleOrderReportSummary emptySummary() {
        return SaleOrderReportSummary.builder()
                .totalOrders(0)
                .toDoCount(0).inProgressCount(0).completeCount(0).voidCount(0).rejectedCount(0)
                .bookingCount(0).depositCount(0).paidCount(0).expiredCount(0)
                .totalDepositAmount(BigDecimal.ZERO).totalFullyPaidAmount(BigDecimal.ZERO).totalBookingAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO).totalPaid(BigDecimal.ZERO).totalRemaining(BigDecimal.ZERO).totalDiscount(BigDecimal.ZERO)
                .build();
    }
}

