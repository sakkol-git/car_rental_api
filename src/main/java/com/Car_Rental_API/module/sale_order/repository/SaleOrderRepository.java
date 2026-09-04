package com.Car_Rental_API.module.sale_order.repository;

import com.db_access.jooq.tables.Provinces;
import com.db_access.jooq.tables.records.SalesOrdersRecord;
import com.db_access.jooq.tables.records.SalesOrderTripsRecord;
import com.db_access.jooq.tables.records.SalesOrderTripSubLocationsRecord;







import com.Car_Rental_API.module.sale_order.model.TripSubLocationItem;
import com.Car_Rental_API.module.sale_order.model.SaleOrderPaymentHistory;
import static com.db_access.jooq.tables.SubLocations.SUB_LOCATIONS;
import static com.db_access.jooq.tables.SalesOrderTripSubLocations.SALES_ORDER_TRIP_SUB_LOCATIONS;
import static com.db_access.jooq.tables.SalesOrderPaymentHistories.SALES_ORDER_PAYMENT_HISTORIES;
import static com.db_access.jooq.tables.SalesOrderTrips.SALES_ORDER_TRIPS;
import static com.db_access.jooq.tables.Provinces.PROVINCES;
import static com.db_access.jooq.tables.SalesOrders.SALES_ORDERS;
import static com.db_access.jooq.tables.Customers.CUSTOMERS;
import static com.db_access.jooq.tables.Vehicles.VEHICLES;
import static com.db_access.jooq.tables.VehicleCategories.VEHICLE_CATEGORIES;
import static com.db_access.jooq.tables.VehicleModels.VEHICLE_MODELS;
import static com.db_access.jooq.tables.VehicleRentalTypes.VEHICLE_RENTAL_TYPES;
import static com.db_access.jooq.tables.Nationalities.NATIONALITIES;
import static com.Car_Rental_API.common.util.QueryUtil.*;






import com.Car_Rental_API.module.sale_order.dto.request.SaleOrderFilterRequest;
import com.Car_Rental_API.module.sale_order.model.SaleOrderTrip;
import com.Car_Rental_API.module.sale_order.model.SaleOrder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SelectFieldOrAsterisk;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SaleOrderRepository {

    private final DSLContext dsl;

    // * Build select fields with relational joins & audit trail
    private List<SelectFieldOrAsterisk> orderFields() {
        List<SelectFieldOrAsterisk> fields = new ArrayList<>(Arrays.asList(SALES_ORDERS.fields()));
        fields.add(CUSTOMERS.FULL_NAME.as("customerName"));
        fields.add(CUSTOMERS.PHONE_NUMBER.as("customerPhone"));
        fields.add(VEHICLES.NAME_EN.as("vehicleName"));
        fields.add(VEHICLES.FILE_NAME.as("vehicleFileName"));
        fields.add(VEHICLES.FILE_URL.as("vehicleFileUrl"));
        fields.add(VEHICLES.MODEL_ID.as("vehicleModelId"));
        fields.add(VEHICLE_MODELS.NAME.as("vehicleModelName"));
        fields.add(SALES_ORDERS.VEHICLE_CATEGORY_ID.as("vehicleCategoryId"));
        fields.add(VEHICLE_CATEGORIES.NAME_KH.as("vehicleCategoryNameKh"));
        fields.add(VEHICLE_CATEGORIES.NAME_EN.as("vehicleCategoryNameEn"));
        fields.add(VEHICLE_CATEGORIES.NAME_ZH.as("vehicleCategoryNameZh"));
        fields.add(VEHICLE_RENTAL_TYPES.NAME_EN.as("vehicleRentalTypeName"));
        fields.add(NATIONALITIES.NAME.as("nationalityName"));
        fields.addAll(auditFields());
        return fields;
    }

    // * Base select query with all standard joins
    private org.jooq.SelectOnConditionStep<?> baseOrderQuery() {
        return addAuditJoins(dsl.select(orderFields()).from(SALES_ORDERS), SALES_ORDERS.getName())
                .leftJoin(CUSTOMERS).on(CUSTOMERS.ID.eq(SALES_ORDERS.CUSTOMER_ID))
                .leftJoin(VEHICLES).on(VEHICLES.ID.eq(SALES_ORDERS.VEHICLE_ID))
                .leftJoin(VEHICLE_MODELS).on(VEHICLE_MODELS.ID.eq(VEHICLES.MODEL_ID))
                .leftJoin(VEHICLE_CATEGORIES).on(VEHICLE_CATEGORIES.ID.eq(SALES_ORDERS.VEHICLE_CATEGORY_ID))
                .leftJoin(VEHICLE_RENTAL_TYPES).on(VEHICLE_RENTAL_TYPES.ID.eq(SALES_ORDERS.VEHICLE_RENTAL_TYPE_ID))
                .leftJoin(NATIONALITIES).on(NATIONALITIES.ID.eq(SALES_ORDERS.PASSENGER_NATIONALITY_ID));
    }

    // * Build dynamic filter conditions for sale orders
    private Condition buildCondition(SaleOrderFilterRequest req) {
        Condition cond = SALES_ORDERS.IS_ACTIVE.eq((byte) 1);
        if (req == null) return cond;

        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            String like = "%" + req.getKeyword().trim() + "%";
            cond = cond.and(
                    SALES_ORDERS.ORDER_NO.likeIgnoreCase(like)
                            .or(SALES_ORDERS.PASSENGER_NAME.likeIgnoreCase(like))
                            .or(SALES_ORDERS.PASSENGER_PHONE.likeIgnoreCase(like))
                            .or(CUSTOMERS.FULL_NAME.likeIgnoreCase(like))
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
            cond = cond.and(SALES_ORDERS.CUSTOMER_ID.eq(req.getCustomerId()).or(SALES_ORDERS.CREATED_BY.eq(req.getCreatedBy())));
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

    // * Query all sale orders with pagination and filtering
    public List<SaleOrder> findAll(SaleOrderFilterRequest req) {
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        List<SaleOrder> orders = baseOrderQuery()
                .where(buildCondition(req))
                .orderBy(SALES_ORDERS.ID.desc())
                .limit(limit)
                .offset(offset)
                .fetchInto(SaleOrder.class);

        orders.forEach(this::loadTrips);
        return orders;
    }

    // * Count all sale orders for pagination
    public long countAll(SaleOrderFilterRequest req) {
        return dsl.selectCount()
                .from(SALES_ORDERS)
                .leftJoin(CUSTOMERS).on(CUSTOMERS.ID.eq(SALES_ORDERS.CUSTOMER_ID))
                .where(buildCondition(req))
                .fetchOne(0, long.class);
    }

    // * Find sale order by ID
    public Optional<SaleOrder> findById(Long id) {
        Optional<SaleOrder> orderOpt = baseOrderQuery()
                .where(SALES_ORDERS.ID.eq(id).and(SALES_ORDERS.IS_ACTIVE.eq((byte) 1)))
                .fetchOptionalInto(SaleOrder.class);
        orderOpt.ifPresent(this::loadOrderDetails);
        return orderOpt;
    }

    // * Find sale order by order number or numeric identifier
    public Optional<SaleOrder> findByOrderNo(String identifier) {
        if (identifier == null || identifier.isBlank()) return Optional.empty();

        org.jooq.Condition cond = SALES_ORDERS.ORDER_NO.eq(identifier.trim());
        try {
            Long numericVal = Long.parseLong(identifier.trim());
            cond = cond.or(SALES_ORDERS.TRANSACTION_ID.eq(numericVal.toString())).or(SALES_ORDERS.ID.eq(numericVal));
        } catch (NumberFormatException ignored) {}

        Optional<SaleOrder> orderOpt = baseOrderQuery()
                .where(cond.and(SALES_ORDERS.IS_ACTIVE.eq((byte) 1)))
                .fetchOptionalInto(SaleOrder.class);
        orderOpt.ifPresent(this::loadOrderDetails);
        return orderOpt;
    }

    // * Find sale orders by customer ID
    public List<SaleOrder> findByCustomerId(Long customerId) {
        SaleOrderFilterRequest req = new SaleOrderFilterRequest();
        req.setCustomerId(customerId);
        req.setSize(100);
        return findAll(req);
    }

    // * Load relational details (trips and payment histories)
    private void loadOrderDetails(SaleOrder order) {
        loadTrips(order);
        loadPaymentHistories(order);
    }

    private void loadTrips(SaleOrder order) {
        Provinces fromProv = PROVINCES.as("fp");
        Provinces toProv = PROVINCES.as("tp");

        List<SelectFieldOrAsterisk> tripFields = new ArrayList<>(Arrays.asList(SALES_ORDER_TRIPS.fields()));
        tripFields.add(fromProv.NAME.as("fromProvinceName"));
        tripFields.add(toProv.NAME.as("toProvinceName"));

        List<SaleOrderTrip> trips = dsl.select(tripFields)
                .from(SALES_ORDER_TRIPS)
                .leftJoin(fromProv).on(fromProv.ID.eq(SALES_ORDER_TRIPS.FROM_PROVINCE_ID))
                .leftJoin(toProv).on(toProv.ID.eq(SALES_ORDER_TRIPS.TO_PROVINCE_ID))
                .where(SALES_ORDER_TRIPS.SALES_ORDER_ID.eq(order.getId()).and(SALES_ORDER_TRIPS.IS_ACTIVE.eq((byte) 1)))
                .orderBy(SALES_ORDER_TRIPS.SORT_ORDER.asc(), SALES_ORDER_TRIPS.ID.asc())
                .fetchInto(SaleOrderTrip.class);

        for (SaleOrderTrip trip : trips) {
            loadTripSubLocations(trip);
        }

        order.setTrips(trips);
    }

    private void loadPaymentHistories(SaleOrder order) {
        if (order == null || order.getId() == null) return;
        List<SaleOrderPaymentHistory> histories = dsl.selectFrom(SALES_ORDER_PAYMENT_HISTORIES)
                .where(SALES_ORDER_PAYMENT_HISTORIES.SALES_ORDER_ID.eq(order.getId())
                        .and(SALES_ORDER_PAYMENT_HISTORIES.IS_ACTIVE.eq((byte) 1)))
                .orderBy(SALES_ORDER_PAYMENT_HISTORIES.ID.asc())
                .fetchInto(SaleOrderPaymentHistory.class);
        order.setPaymentHistories(histories);
    }

    private void loadTripSubLocations(SaleOrderTrip trip) {
        if (trip == null || trip.getId() == null) return;
        List<TripSubLocationItem> subLocs = dsl.select(
                        SALES_ORDER_TRIP_SUB_LOCATIONS.ID.as("id"),
                        SALES_ORDER_TRIP_SUB_LOCATIONS.TRIP_ID.as("tripId"),
                        SALES_ORDER_TRIP_SUB_LOCATIONS.SUB_LOCATION_ID.as("subLocationId"),
                        SUB_LOCATIONS.NAME.as("subLocationName"),
                        SALES_ORDER_TRIP_SUB_LOCATIONS.PRICE.as("price"),
                        SALES_ORDER_TRIP_SUB_LOCATIONS.SORT_ORDER.as("sortOrder")
                )
                .from(SALES_ORDER_TRIP_SUB_LOCATIONS)
                .join(SUB_LOCATIONS).on(SUB_LOCATIONS.ID.eq(SALES_ORDER_TRIP_SUB_LOCATIONS.SUB_LOCATION_ID))
                .where(SALES_ORDER_TRIP_SUB_LOCATIONS.TRIP_ID.eq(trip.getId())
                        .and(SALES_ORDER_TRIP_SUB_LOCATIONS.IS_ACTIVE.eq((byte) 1)))
                .orderBy(SALES_ORDER_TRIP_SUB_LOCATIONS.SORT_ORDER.asc(), SALES_ORDER_TRIP_SUB_LOCATIONS.ID.asc())
                .fetchInto(TripSubLocationItem.class);

        trip.setSubLocations(subLocs);
    }

    // * Save sale order and initial payment history
    public SaleOrder saveOrder(SaleOrder order, List<SaleOrderTrip> trips) {
        SalesOrdersRecord record = dsl.newRecord(SALES_ORDERS);
        record.from(order);
        record.setId(null);

        if (record.getOrderDate() == null)     record.setOrderDate(LocalDateTime.now());
        if (record.getCreated() == null)       record.setCreated(LocalDateTime.now());
        if (record.getIsActive() == null)      record.setIsActive((byte) 1);
        if (record.getCurrency() == null)      record.setCurrency("USD");
        if (record.getOrderStatus() == null)   record.setOrderStatus((byte) 1);
        if (record.getPaymentStatus() == null)  record.setPaymentStatus((byte) 1);

        dsl.insertInto(SALES_ORDERS).set(record).execute();
        Long orderId = dsl.lastID().longValue();
        order.setId(orderId);

        saveTripsAndSubLocations(orderId, trips);
        order.setTrips(trips);

        // * Always log payment history whenever a payment amount is recorded at order creation
        BigDecimal creationPaid = order.getPaidAmount() != null ? order.getPaidAmount() : BigDecimal.ZERO;
        if (creationPaid.compareTo(BigDecimal.ZERO) > 0) {
            // Use depositPrice as the step amount when set, otherwise use paidAmount
            BigDecimal historyAmount = (order.getDepositPrice() != null && order.getDepositPrice().compareTo(BigDecimal.ZERO) > 0)
                    ? order.getDepositPrice()
                    : creationPaid;
            logPaymentHistory(orderId, order.getPaymentType(), order.getPaymentMethod(), order.getRemainingAmount(),
                    historyAmount, order.getReceiptFileName(), order.getReceiptFileUrl(), order.getReceiptDescription(), order.getPaymentStatus(), order.getCreatedBy());
        }

        return order;
    }

    public void updateOrder(SaleOrder order, List<SaleOrderTrip> trips) {
        updateOrder(order, trips, null, null);
    }

    public void updateOrder(SaleOrder order, List<SaleOrderTrip> trips, BigDecimal oldTotal, Long userId) {
        SalesOrdersRecord record = dsl.newRecord(SALES_ORDERS);
        record.from(order);
        record.setModified(LocalDateTime.now());
        if (userId != null) record.setModifiedBy(userId);

        dsl.update(SALES_ORDERS)
                .set(record)
                .where(SALES_ORDERS.ID.eq(order.getId()))
                .execute();

        deleteTripsAndSubLocations(order.getId());
        saveTripsAndSubLocations(order.getId(), trips);

        if (oldTotal != null && order.getTotalAmount() != null && order.getTotalAmount().compareTo(oldTotal) != 0) {
            logPriceRevisionHistory(order.getId(), oldTotal, order.getTotalAmount(), order.getRemainingAmount(), userId);
        }

        // * Log initial payment history on update if payment or receipt was submitted and no payment stage recorded yet
        boolean hasPaymentEvent = (order.getReceiptFileName() != null || order.getReceiptFileUrl() != null)
                || (order.getPaidAmount() != null && order.getPaidAmount().compareTo(BigDecimal.ZERO) > 0);
        if (hasPaymentEvent) {
            boolean hasHistory = dsl.fetchExists(
                    dsl.selectOne()
                            .from(SALES_ORDER_PAYMENT_HISTORIES)
                            .where(SALES_ORDER_PAYMENT_HISTORIES.SALES_ORDER_ID.eq(order.getId())
                                    .and(SALES_ORDER_PAYMENT_HISTORIES.IS_ACTIVE.eq((byte) 1))
                                    .and(SALES_ORDER_PAYMENT_HISTORIES.PAYMENT_STAGE.ne((byte) 4)))
            );
            if (!hasHistory) {
                BigDecimal historyAmount = (order.getDepositPrice() != null && order.getDepositPrice().compareTo(BigDecimal.ZERO) > 0)
                        ? order.getDepositPrice()
                        : (order.getPaidAmount() != null ? order.getPaidAmount() : BigDecimal.ZERO);
                logPaymentHistory(order.getId(), order.getPaymentType(), order.getPaymentMethod(), order.getRemainingAmount(),
                        historyAmount, order.getReceiptFileName(), order.getReceiptFileUrl(), order.getReceiptDescription(), order.getPaymentStatus(), userId);
            }
        }
    }

    // * Update order status with optional void remark and audit tracking
    public void updateOrderStatus(Long id, Byte status) { updateOrderStatus(id, status, null, null); }
    public void updateOrderStatus(Long id, Byte status, Long userId) { updateOrderStatus(id, status, null, userId); }

    public void updateOrderStatus(Long id, Byte status, String voidRemark, Long userId) {
        var update = dsl.update(SALES_ORDERS)
                .set(SALES_ORDERS.ORDER_STATUS, status)
                .set(SALES_ORDERS.MODIFIED, LocalDateTime.now())
                .set(SALES_ORDERS.MODIFIED_BY, userId);
        if (status != null && status == 4 && voidRemark != null && !voidRemark.isBlank()) {
            update = update.set(SALES_ORDERS.VOID_REMARK, voidRemark.trim());
        }
        update.where(SALES_ORDERS.ID.eq(id)).execute();
    }

    public void voidOrder(Long id, String voidRemark, Long userId) {
        updateOrderStatus(id, (byte) 4, voidRemark, userId);
    }

    // * Auto-advance active paid/deposit orders starting today to In Progress (1 -> 2)
    public int autoAdvanceOrdersToInProgress() {
        return dsl.update(SALES_ORDERS)
                .set(SALES_ORDERS.ORDER_STATUS, (byte) 2)
                .set(SALES_ORDERS.MODIFIED, LocalDateTime.now())
                .where(SALES_ORDERS.IS_ACTIVE.eq((byte) 1))
                .and(SALES_ORDERS.ORDER_STATUS.eq((byte) 1))
                .and(SALES_ORDERS.START_DATE.lessOrEqual(LocalDate.now()))
                .and(SALES_ORDERS.PAYMENT_STATUS.in((byte) 2, (byte) 3))
                .execute();
    }

    // * Auto-advance in-progress orders whose end date has passed to Complete (2 -> 3)
    public int autoAdvanceOrdersToComplete() {
        return dsl.update(SALES_ORDERS)
                .set(SALES_ORDERS.ORDER_STATUS, (byte) 3)
                .set(SALES_ORDERS.MODIFIED, LocalDateTime.now())
                .where(SALES_ORDERS.IS_ACTIVE.eq((byte) 1))
                .and(SALES_ORDERS.ORDER_STATUS.eq((byte) 2))
                .and(org.jooq.impl.DSL.coalesce(SALES_ORDERS.END_DATE, SALES_ORDERS.START_DATE).lessThan(LocalDate.now()))
                .execute();
    }

    // * Soft delete sale order
    public void deleteById(Long id, Long userId) {
        dsl.update(SALES_ORDERS)
                .set(SALES_ORDERS.IS_ACTIVE, (byte) 0)
                .set(SALES_ORDERS.MODIFIED, LocalDateTime.now())
                .set(SALES_ORDERS.MODIFIED_BY, userId)
                .where(SALES_ORDERS.ID.eq(id))
                .execute();
    }

    // * Update payment summary on main order and log step in payment history
    public void updateReceipt(Long id, Byte paymentType, Byte paymentStatus, Integer paymentMethod,
                              BigDecimal paidAmount, BigDecimal depositPrice, BigDecimal discountAmount,
                              BigDecimal totalAmount, BigDecimal remainingAmount,
                              String fileName, String fileUrl, String description,
                              BigDecimal stepAmount, BigDecimal oldTotal, Long userId) {
        var update = dsl.update(SALES_ORDERS)
                .set(SALES_ORDERS.MODIFIED, LocalDateTime.now());
        if (userId != null)           update = update.set(SALES_ORDERS.MODIFIED_BY, userId);
        if (paymentType != null)      update = update.set(SALES_ORDERS.PAYMENT_TYPE, paymentType);
        if (paymentStatus != null)    update = update.set(SALES_ORDERS.PAYMENT_STATUS, paymentStatus);
        if (paymentMethod != null)    update = update.set(SALES_ORDERS.PAYMENT_METHOD, paymentMethod.byteValue());
        if (discountAmount != null)   update = update.set(SALES_ORDERS.DISCOUNT_AMOUNT, discountAmount);
        if (totalAmount != null)      update = update.set(SALES_ORDERS.TOTAL_AMOUNT, totalAmount);
        if (paidAmount != null)       update = update.set(SALES_ORDERS.PAID_AMOUNT, paidAmount);
        if (depositPrice != null)     update = update.set(SALES_ORDERS.DEPOSIT_PRICE, depositPrice);
        if (remainingAmount != null)  update = update.set(SALES_ORDERS.REMAINING_AMOUNT, remainingAmount);
        if (fileName != null)         update = update.set(SALES_ORDERS.RECEIPT_FILE_NAME, fileName);
        if (fileUrl != null)          update = update.set(SALES_ORDERS.RECEIPT_FILE_URL, fileUrl);
        if (description != null)      update = update.set(SALES_ORDERS.RECEIPT_DESCRIPTION, description);
        update.where(SALES_ORDERS.ID.eq(id)).execute();

        // * Write Price Revision audit row when total amount changes
        if (oldTotal != null && totalAmount != null && totalAmount.compareTo(oldTotal) != 0) {
            logPriceRevisionHistory(id, oldTotal, totalAmount, remainingAmount, userId);
        }

        // * Log payment history whenever receipt or payment amount is recorded
        boolean hasPaymentEvent = (fileName != null || fileUrl != null)
                || (stepAmount != null && stepAmount.compareTo(BigDecimal.ZERO) > 0);

        if (hasPaymentEvent) {
            BigDecimal historyAmount = (stepAmount != null && stepAmount.compareTo(BigDecimal.ZERO) > 0)
                    ? stepAmount
                    : (paidAmount != null ? paidAmount : BigDecimal.ZERO);
            logPaymentHistory(id, paymentType, paymentMethod, remainingAmount, historyAmount, fileName, fileUrl, description, paymentStatus, userId);
        }
    }

    // * Log payment step in sales_order_payment_histories audit table
    private void logPaymentHistory(Long orderId, Byte paymentType, Integer paymentMethod, BigDecimal remainingAmount,
                                   BigDecimal amount, String fileName, String fileUrl, String description, Byte status, Long userId) {
        boolean isFirstPayment = !dsl.fetchExists(
                dsl.selectOne()
                        .from(SALES_ORDER_PAYMENT_HISTORIES)
                        .where(SALES_ORDER_PAYMENT_HISTORIES.SALES_ORDER_ID.eq(orderId)
                                .and(SALES_ORDER_PAYMENT_HISTORIES.IS_ACTIVE.eq((byte) 1))
                                .and(SALES_ORDER_PAYMENT_HISTORIES.PAYMENT_STAGE.ne((byte) 4)))
        );

        boolean isFullyPaid = remainingAmount != null && remainingAmount.compareTo(BigDecimal.ZERO) == 0;
        byte stage = isFirstPayment
                ? (isFullyPaid ? (byte) 3 : (byte) 1)
                : (isFullyPaid ? (byte) 2 : (byte) 1);

        dsl.insertInto(SALES_ORDER_PAYMENT_HISTORIES)
                .set(SALES_ORDER_PAYMENT_HISTORIES.SALES_ORDER_ID, orderId)
                .set(SALES_ORDER_PAYMENT_HISTORIES.PAYMENT_TYPE, paymentType != null ? paymentType : (byte) 1)
                .set(SALES_ORDER_PAYMENT_HISTORIES.PAYMENT_METHOD, paymentMethod != null ? paymentMethod.byteValue() : (byte) 1)
                .set(SALES_ORDER_PAYMENT_HISTORIES.PAYMENT_STAGE, stage)
                .set(SALES_ORDER_PAYMENT_HISTORIES.AMOUNT, amount != null ? amount : BigDecimal.ZERO)
                .set(SALES_ORDER_PAYMENT_HISTORIES.RECEIPT_FILE_NAME, fileName)
                .set(SALES_ORDER_PAYMENT_HISTORIES.RECEIPT_FILE_URL, fileUrl)
                .set(SALES_ORDER_PAYMENT_HISTORIES.RECEIPT_DESCRIPTION, description)
                .set(SALES_ORDER_PAYMENT_HISTORIES.CREATED, LocalDateTime.now())
                .set(SALES_ORDER_PAYMENT_HISTORIES.CREATED_BY, userId)
                .set(SALES_ORDER_PAYMENT_HISTORIES.STATUS, status != null ? status.byteValue() : (byte) 2)
                .set(SALES_ORDER_PAYMENT_HISTORIES.IS_ACTIVE, (byte) 1)
                .execute();
    }

    // * Replace trips for an order and update order financial totals
    public void updateTripsAndOrderFinancials(Long orderId, List<SaleOrderTrip> trips,
                                              BigDecimal subtotal, BigDecimal serviceFee, BigDecimal total,
                                              BigDecimal remaining, Byte paymentStatus,
                                              BigDecimal oldTotal, Long userId) {
        deleteTripsAndSubLocations(orderId);
        saveTripsAndSubLocations(orderId, trips);

        dsl.update(SALES_ORDERS)
                .set(SALES_ORDERS.SUBTOTAL_AMOUNT, subtotal)
                .set(SALES_ORDERS.SERVICE_FEE, serviceFee)
                .set(SALES_ORDERS.TOTAL_AMOUNT, total)
                .set(SALES_ORDERS.REMAINING_AMOUNT, remaining)
                .set(SALES_ORDERS.PAYMENT_STATUS, paymentStatus)
                .set(SALES_ORDERS.MODIFIED, LocalDateTime.now())
                .set(SALES_ORDERS.MODIFIED_BY, userId)
                .where(SALES_ORDERS.ID.eq(orderId))
                .execute();

        // * Write Price Revision audit row when total amount changes
        if (oldTotal != null && total.compareTo(oldTotal) != 0) {
            logPriceRevisionHistory(orderId, oldTotal, total, remaining, userId);
        }
    }

    // * Append a Stage-4 (Price Revision) row to payment history — no money exchanged, purely informational
    private void logPriceRevisionHistory(Long orderId, BigDecimal oldTotal, BigDecimal newTotal, BigDecimal newRemaining, Long userId) {
        BigDecimal delta = newTotal.subtract(oldTotal); // positive = price increase, negative = price decrease
        String desc = "Trip revision: total changed from " + oldTotal.stripTrailingZeros().toPlainString()
                + " → " + newTotal.stripTrailingZeros().toPlainString()
                + (delta.compareTo(BigDecimal.ZERO) > 0
                ? " (+" + delta.stripTrailingZeros().toPlainString() + ")"
                : " (" + delta.stripTrailingZeros().toPlainString() + ")");

        dsl.insertInto(SALES_ORDER_PAYMENT_HISTORIES)
                .set(SALES_ORDER_PAYMENT_HISTORIES.SALES_ORDER_ID, orderId)
                .set(SALES_ORDER_PAYMENT_HISTORIES.PAYMENT_TYPE,  (byte) 0)  // 0: N/A — no money exchanged
                .set(SALES_ORDER_PAYMENT_HISTORIES.PAYMENT_METHOD, (byte) 0) // 0: N/A
                .set(SALES_ORDER_PAYMENT_HISTORIES.PAYMENT_STAGE, (byte) 4)  // 4: Price Revision
                .set(SALES_ORDER_PAYMENT_HISTORIES.AMOUNT, delta)
                .set(SALES_ORDER_PAYMENT_HISTORIES.RECEIPT_DESCRIPTION, desc)
                .set(SALES_ORDER_PAYMENT_HISTORIES.CREATED, LocalDateTime.now())
                .set(SALES_ORDER_PAYMENT_HISTORIES.CREATED_BY, userId)
                .set(SALES_ORDER_PAYMENT_HISTORIES.STATUS, (byte) 2)  // 2: informational / active
                .set(SALES_ORDER_PAYMENT_HISTORIES.IS_ACTIVE, (byte) 1)
                .execute();
    }

    // * Replace trips for an order without touching any other order fields
    public void updateTripsOnly(Long orderId, List<SaleOrderTrip> trips) {
        deleteTripsAndSubLocations(orderId);
        saveTripsAndSubLocations(orderId, trips);
    }

    private void deleteTripsAndSubLocations(Long orderId) {
        List<Long> tripIds = dsl.select(SALES_ORDER_TRIPS.ID)
                .from(SALES_ORDER_TRIPS)
                .where(SALES_ORDER_TRIPS.SALES_ORDER_ID.eq(orderId))
                .fetchInto(Long.class);

        if (!tripIds.isEmpty()) {
            dsl.deleteFrom(SALES_ORDER_TRIP_SUB_LOCATIONS)
                    .where(SALES_ORDER_TRIP_SUB_LOCATIONS.TRIP_ID.in(tripIds))
                    .execute();
        }

        dsl.deleteFrom(SALES_ORDER_TRIPS)
                .where(SALES_ORDER_TRIPS.SALES_ORDER_ID.eq(orderId))
                .execute();
    }

    private void saveTripsAndSubLocations(Long orderId, List<SaleOrderTrip> trips) {
        if (trips == null || trips.isEmpty()) return;

        for (int i = 0; i < trips.size(); i++) {
            SaleOrderTrip trip = trips.get(i);
            SalesOrderTripsRecord tripRecord = dsl.newRecord(SALES_ORDER_TRIPS);
            tripRecord.from(trip);
            tripRecord.setId(null);
            tripRecord.setSalesOrderId(orderId);
            tripRecord.setSortOrder(i + 1);
            tripRecord.setStatus(trip.getStatus() != null ? trip.getStatus().byteValue() : (byte) 3);
            tripRecord.setCreated(LocalDateTime.now());
            tripRecord.setIsActive((byte) 1);
            dsl.insertInto(SALES_ORDER_TRIPS).set(tripRecord).execute();
            Long tripId = dsl.lastID().longValue();
            trip.setId(tripId);
            trip.setSalesOrderId(orderId);

            if (trip.getSubLocations() != null && !trip.getSubLocations().isEmpty()) {
                for (int j = 0; j < trip.getSubLocations().size(); j++) {
                    TripSubLocationItem item = trip.getSubLocations().get(j);
                    if (item == null || item.getSubLocationId() == null) continue;

                    SalesOrderTripSubLocationsRecord subRec = dsl.newRecord(SALES_ORDER_TRIP_SUB_LOCATIONS);
                    subRec.setTripId(tripId);
                    subRec.setSubLocationId(item.getSubLocationId());
                    subRec.setPrice(item.getPrice());
                    subRec.setSortOrder(item.getSortOrder() != null ? item.getSortOrder() : j + 1);
                    subRec.setIsActive((byte) 1);
                    dsl.insertInto(SALES_ORDER_TRIP_SUB_LOCATIONS).set(subRec).execute();
                    item.setId(dsl.lastID().longValue());
                    item.setTripId(tripId);
                }
            }
        }
    }

    // * Order number format: {2-digit year}SO{6-digit year-sequence} — e.g. 26SO000001
    public String generateOrderNo() {
        String yy = LocalDate.now().format(DateTimeFormatter.ofPattern("yy"));
        String prefix = yy + "SO";
        Long countThisYear = dsl.selectCount()
                .from(SALES_ORDERS)
                .where(SALES_ORDERS.ORDER_NO.like(prefix + "%"))
                .fetchOne(0, long.class);

        return prefix + String.format("%06d", countThisYear + 1);
    }

    // * Calculate peak daily booked vehicles count within requested date range
    public int getMaxBookedVehiclesInDateRange(Long vehicleId, LocalDate startDate, LocalDate endDate, Long excludeOrderId) {
        if (vehicleId == null || startDate == null || endDate == null) return 0;

        var cond = SALES_ORDERS.VEHICLE_ID.eq(vehicleId)
                .and(SALES_ORDERS.IS_ACTIVE.eq((byte) 1))
                .and(SALES_ORDERS.ORDER_STATUS.ne((byte) 4)) // 4: Void
                .and(SALES_ORDERS.START_DATE.lessOrEqual(endDate))
                .and(SALES_ORDERS.END_DATE.greaterOrEqual(startDate));

        if (excludeOrderId != null) cond = cond.and(SALES_ORDERS.ID.ne(excludeOrderId));

        var records = dsl.select(SALES_ORDERS.START_DATE, SALES_ORDERS.END_DATE, SALES_ORDERS.AMOUNT_OF_VEHICLES)
                .from(SALES_ORDERS).where(cond).fetch();
        if (records.isEmpty()) return 0;

        int maxBooked = 0;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            final LocalDate current = date;
            int dailyBooked = records.stream()
                    .filter(r -> !current.isBefore(r.get(SALES_ORDERS.START_DATE)) && !current.isAfter(r.get(SALES_ORDERS.END_DATE)))
                    .mapToInt(r -> Optional.ofNullable(r.get(SALES_ORDERS.AMOUNT_OF_VEHICLES)).filter(q -> q > 0).orElse(1))
                    .sum();
            maxBooked = Math.max(maxBooked, dailyBooked);
        }

        return maxBooked;
    }
}
