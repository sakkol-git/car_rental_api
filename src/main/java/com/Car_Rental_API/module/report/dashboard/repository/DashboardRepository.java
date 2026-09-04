package com.Car_Rental_API.module.report.dashboard.repository;

import com.Car_Rental_API.common.base_dto.request.DueDateFilterRequest;
import com.Car_Rental_API.module.report.dashboard.dto.response.DashboardResponse;
import com.Car_Rental_API.module.report.dashboard.dto.response.DashboardResponse.BookingTrend;
import com.Car_Rental_API.module.report.dashboard.dto.response.DashboardResponse.FleetDistribution;
import com.Car_Rental_API.module.report.dashboard.dto.response.DashboardResponse.RecentOrder;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import static com.db_access.jooq.tables.Customers.CUSTOMERS;
import static com.db_access.jooq.tables.SalesOrders.SALES_ORDERS;
import static com.db_access.jooq.tables.VehicleCategories.VEHICLE_CATEGORIES;
import static com.db_access.jooq.tables.VehicleCategoriesMapping.VEHICLE_CATEGORIES_MAPPING;
import static com.db_access.jooq.tables.Vehicles.VEHICLES;

@Repository
@RequiredArgsConstructor
public class DashboardRepository {

    private final DSLContext dsl;

    private Condition buildOrderCondition(DueDateFilterRequest req) {
        Condition cond = SALES_ORDERS.IS_ACTIVE.eq((byte) 1);
        if (req == null) return cond;

        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            String like = "%" + req.getKeyword().trim() + "%";
            cond = cond.and(
                SALES_ORDERS.ORDER_NO.likeIgnoreCase(like)
                    .or(CUSTOMERS.FULL_NAME.likeIgnoreCase(like))
                    .or(CUSTOMERS.PHONE_NUMBER.likeIgnoreCase(like))
                    .or(VEHICLES.NAME_EN.likeIgnoreCase(like))
            );
        }
        if (req.getDateFrom() != null && !req.getDateFrom().isBlank()) {
            cond = cond.and(SALES_ORDERS.START_DATE.greaterOrEqual(LocalDate.parse(req.getDateFrom())));
        }
        if (req.getDateTo() != null && !req.getDateTo().isBlank()) {
            cond = cond.and(SALES_ORDERS.END_DATE.lessOrEqual(LocalDate.parse(req.getDateTo())));
        }
        return cond;
    }

    // * Full Dashboard summary (KPIs + Trends + Recent Orders + Fleet Distribution)
    public DashboardResponse buildSummary(DueDateFilterRequest req) {
        DashboardResponse response = new DashboardResponse();
        Condition cond = buildOrderCondition(req);

        response.setTotalOrders(countOrders(cond, null));
        response.setTotalToDo(countOrders(cond, (byte) 1));
        response.setTotalInProgress(countOrders(cond, (byte) 2));
        response.setTotalComplete(countOrders(cond, (byte) 3));

        BigDecimal revenue = dsl.select(DSL.coalesce(DSL.sum(SALES_ORDERS.TOTAL_AMOUNT), BigDecimal.ZERO))
                .from(SALES_ORDERS)
                .leftJoin(CUSTOMERS).on(CUSTOMERS.ID.eq(SALES_ORDERS.CUSTOMER_ID))
                .leftJoin(VEHICLES).on(VEHICLES.ID.eq(SALES_ORDERS.VEHICLE_ID))
                .where(cond.and(SALES_ORDERS.ORDER_STATUS.eq((byte) 3)))
                .fetchOne(0, BigDecimal.class);
        response.setTotalRevenue(revenue != null ? revenue : BigDecimal.ZERO);

        response.setBookingTrends(getBookingTrends(req));
        response.setRecentOrders(getRecentOrders(req));
        response.setFleetDistributions(getFleetDistribution(req));

        return response;
    }

    // * Endpoint 1: Booking Trends chart (12 months of current year)
    public List<BookingTrend> getBookingTrends(DueDateFilterRequest req) {
        int year = LocalDate.now().getYear();
        Condition cond = buildOrderCondition(req);

        return dsl.select(
                DSL.month(SALES_ORDERS.ORDER_DATE).as("monthNum"),
                DSL.count().as("count")
        )
        .from(SALES_ORDERS)
        .leftJoin(CUSTOMERS).on(CUSTOMERS.ID.eq(SALES_ORDERS.CUSTOMER_ID))
        .leftJoin(VEHICLES).on(VEHICLES.ID.eq(SALES_ORDERS.VEHICLE_ID))
        .where(cond.and(DSL.year(SALES_ORDERS.ORDER_DATE).eq(year)))
        .groupBy(DSL.month(SALES_ORDERS.ORDER_DATE))
        .orderBy(DSL.month(SALES_ORDERS.ORDER_DATE).asc())
        .fetch(r -> {
            BookingTrend t = new BookingTrend();
            Number monthNum = (Number) r.get("monthNum");
            int month = monthNum != null ? monthNum.intValue() : 1;
            t.setMonth(LocalDate.of(year, month, 1).getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            t.setYear(year);
            Number cntNum = (Number) r.get("count");
            t.setCount(cntNum != null ? cntNum.longValue() : 0L);
            return t;
        });
    }

    // * Endpoint 2: Recent 10 orders table
    public List<RecentOrder> getRecentOrders(DueDateFilterRequest req) {
        Condition cond = buildOrderCondition(req);

        return dsl.select(
                SALES_ORDERS.ORDER_NO,
                CUSTOMERS.FULL_NAME.as("customerName"),
                CUSTOMERS.PHONE_NUMBER.as("customerPhone"),
                VEHICLES.NAME_EN.as("vehicleName"),
                SALES_ORDERS.AMOUNT_OF_VEHICLES,
                SALES_ORDERS.ORDER_STATUS
        )
        .from(SALES_ORDERS)
        .leftJoin(CUSTOMERS).on(CUSTOMERS.ID.eq(SALES_ORDERS.CUSTOMER_ID))
        .leftJoin(VEHICLES).on(VEHICLES.ID.eq(SALES_ORDERS.VEHICLE_ID))
        .where(cond)
        .orderBy(SALES_ORDERS.ID.desc())
        .limit(10)
        .fetchInto(RecentOrder.class);
    }

    // * Endpoint 3: Fleet Distribution by category (donut chart)
    public List<FleetDistribution> getFleetDistribution(DueDateFilterRequest req) {
        long totalVehicles = dsl.selectCount()
                .from(VEHICLES)
                .where(VEHICLES.IS_ACTIVE.eq((byte) 1))
                .fetchOne(0, long.class);

        if (totalVehicles == 0) totalVehicles = 1L;
        final long grandTotal = totalVehicles;

        return dsl.select(
                VEHICLE_CATEGORIES.ID.as("vehicleCategoryId"),
                VEHICLE_CATEGORIES.NAME_EN.as("vehicleCategoryName"),
                DSL.count(VEHICLES.ID).as("vehicleCount")
        )
        .from(VEHICLE_CATEGORIES)
        .leftJoin(VEHICLE_CATEGORIES_MAPPING).on(VEHICLE_CATEGORIES_MAPPING.CATEGORY_ID.eq(VEHICLE_CATEGORIES.ID))
        .leftJoin(VEHICLES).on(VEHICLES.ID.eq(VEHICLE_CATEGORIES_MAPPING.VEHICLE_ID).and(VEHICLES.IS_ACTIVE.eq((byte) 1)))
        .where(VEHICLE_CATEGORIES.IS_ACTIVE.eq((byte) 1))
        .groupBy(VEHICLE_CATEGORIES.ID, VEHICLE_CATEGORIES.NAME_EN)
        .fetch(r -> {
            FleetDistribution f = new FleetDistribution();
            Number catIdNum = (Number) r.get("vehicleCategoryId");
            f.setVehicleCategoryId(catIdNum != null ? catIdNum.longValue() : null);
            f.setVehicleCategoryName(r.get("vehicleCategoryName", String.class));
            Number cntNum = (Number) r.get("vehicleCount");
            long cnt = cntNum != null ? cntNum.longValue() : 0L;
            f.setVehicleCount(cnt);
            f.setPercentage(Math.round((cnt * 100.0 / grandTotal) * 100.0) / 100.0);
            return f;
        });
    }

    private long countOrders(Condition cond, Byte orderStatus) {
        var q = dsl.selectCount()
                .from(SALES_ORDERS)
                .leftJoin(CUSTOMERS).on(CUSTOMERS.ID.eq(SALES_ORDERS.CUSTOMER_ID))
                .leftJoin(VEHICLES).on(VEHICLES.ID.eq(SALES_ORDERS.VEHICLE_ID))
                .where(cond);
        if (orderStatus != null) q = q.and(SALES_ORDERS.ORDER_STATUS.eq(orderStatus));
        return q.fetchOne(0, long.class);
    }
}
