package com.Car_Rental_API.module.sale_order.service;

import com.Car_Rental_API.module.payment.dto.response.PaymentResponse;



import com.Car_Rental_API.module.payment.service.PaymentService;
import com.Car_Rental_API.security.authentication.util.AuthSystemWebhookClient;
import com.Car_Rental_API.module.sale_order.dto.request.ReceiptUpdateRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.exception.GlobalException;
import com.Car_Rental_API.common.util.QueryUtil;
import com.Car_Rental_API.module.master_data.customer.model.Customer;
import com.Car_Rental_API.module.master_data.customer.service.CustomerService;
import com.Car_Rental_API.module.sale_order.dto.request.SaleOrderFilterRequest;
import com.Car_Rental_API.module.sale_order.dto.request.SaleOrderRequest;
import com.Car_Rental_API.module.sale_order.dto.request.SaleOrderTripRequest;
import com.Car_Rental_API.module.sale_order.dto.response.MobileBookingCreateResponse;
import com.Car_Rental_API.module.sale_order.dto.response.SaleOrderResponse;
import com.Car_Rental_API.module.sale_order.helper.SaleOrderValidator;
import com.Car_Rental_API.module.sale_order.mapper.SaleOrderMapper;
import com.Car_Rental_API.module.sale_order.model.SaleOrder;
import com.Car_Rental_API.module.sale_order.model.SaleOrderTrip;
import com.Car_Rental_API.module.sale_order.repository.SaleOrderRepository;
import com.Car_Rental_API.security.authentication.auth.dto.AuthUserResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleOrderService {

    private final SaleOrderRepository saleOrderRepository;
    private final SaleOrderValidator saleOrderValidator;
    private final SaleOrderMapper saleOrderMapper;
    private final CustomerService customerService;
    private final AuthSystemWebhookClient authWebhookClient;
    private final PaymentService paymentService;

    // * Query & Search Operations
    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "saleOrders", key = "#req != null ? #req.toString() : 'default'")
    public PageResult<SaleOrderResponse> getAllOrders(SaleOrderFilterRequest req) {
        SaleOrderFilterRequest filter = req != null ? req : new SaleOrderFilterRequest();
        long total = QueryUtil.shouldCount(filter.getPage(), () -> saleOrderRepository.countAll(filter));
        List<SaleOrderResponse> list = saleOrderMapper.toResponses(saleOrderRepository.findAll(filter));
        return new PageResult<>(list, total);
    }

    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "saleOrder", key = "#id")
    public SaleOrder getOrderById(Long id) {
        return saleOrderRepository.findById(id)
                .orElseThrow(() -> new GlobalException("Sale order not found", 404));
    }

    @CircuitBreaker(name = "defaultService")
    public SaleOrderResponse getOrderResponseById(Long id) {
        return saleOrderMapper.toResponse(getOrderById(id));
    }

    // * Mobile App Order Placement (Order Type 1: APP)
    @Transactional
    @CacheEvict(value = {"saleOrders", "saleOrder", "saleOrderReportList", "saleOrderReportSummary"}, allEntries = true)
    public MobileBookingCreateResponse createAppOrder(String rawToken, SaleOrderRequest request) {
        saleOrderValidator.validateOrderRequest(request, null);

        AuthUserResponse authUser = authWebhookClient.verifyAndGetUserInfo(rawToken)
                .orElseThrow(() -> new GlobalException("Invalid or unverified user token", 401));

        String custName = (authUser.getFullName() != null && !authUser.getFullName().isBlank())
                ? authUser.getFullName().trim()
                : (request.getPassengerName() != null ? request.getPassengerName().trim() : "App Customer");

        String custPhone = (authUser.getPhone() != null && !authUser.getPhone().isBlank())
                ? authUser.getPhone().trim()
                : (request.getPassengerPhone() != null ? request.getPassengerPhone().trim() : "");

        Customer customer = customerService.getOrCreateCustomer(parseUserId(authUser.getUserId()), custName, custPhone, authUser.getEmail(), authUser.getOsType());

        SaleOrder order = saleOrderMapper.fromRequest(request);
        order.setCustomerId(customer.getId());
        order.setPassengerName(request.getPassengerName() != null ? request.getPassengerName() : custName);
        order.setPassengerPhone(request.getPassengerPhone() != null ? request.getPassengerPhone() : custPhone);
        order.setOrderType((byte) 1);
        order.setOrderNo(saleOrderRepository.generateOrderNo());
        order.setOrderDate(LocalDateTime.now());
        order.setCreated(LocalDateTime.now());

        // * Auto-calculate payment breakdown & fallback values
        saleOrderValidator.autoCalculateOrderPayment(order, request);

        String tranId = paymentService.generateTransactionId();
        try {
            order.setTransactionId(Long.parseLong(tranId));
        } catch (Exception ignored) {}

        List<SaleOrderTrip> trips = saleOrderValidator.processTripRequests(request.getTrips());
        SaleOrder savedOrder = saleOrderRepository.saveOrder(order, trips);

        Integer method = request.getPaymentMethod() != null ? request.getPaymentMethod() : 1;

        MobileBookingCreateResponse createResponse = MobileBookingCreateResponse.builder()
                .transactionId(tranId)
                .totalAmount(savedOrder.getTotalAmount())
                .currency(savedOrder.getCurrency() != null && !savedOrder.getCurrency().isBlank() ? savedOrder.getCurrency() : "USD")
                .paymentStatus(savedOrder.getPaymentStatus() != null ? savedOrder.getPaymentStatus().intValue() : 1)
                .paymentStatusLabel(method == 8 ? "PENDING_COD" : (savedOrder.getPaymentStatus() == 3 ? "PAID" : (savedOrder.getPaymentStatus() == 2 ? "DEPOSIT" : "BOOKING")))
                .build();

        if (method != 8) {
            try {
                PaymentResponse paymentInfo = paymentService.findQrPaymentForOrder(savedOrder, tranId, method);
                if (paymentInfo != null) {
                    if (paymentInfo.getTransactionId() != null) createResponse.setTransactionId(paymentInfo.getTransactionId());
                    createResponse.setQrString(paymentInfo.getQrString());
                    createResponse.setQrImage(paymentInfo.getQrImage());
                    createResponse.setAbapayDeeplink(paymentInfo.getAbapayUrl());
                    createResponse.setCheckoutQrUrl(paymentInfo.getCheckoutQrUrl());
                    if (paymentInfo.getPaymentStatus() != null) createResponse.setPaymentStatus(paymentInfo.getPaymentStatus());
                    if (paymentInfo.getPaymentStatusLabel() != null) createResponse.setPaymentStatusLabel(paymentInfo.getPaymentStatusLabel());
                }
            } catch (Exception e) {
                log.warn("[createAppOrder] Pre-generating payment info failed for tranId={}: {}", tranId, e.getMessage());
            }
        }

        return createResponse;
    }

    // * System Admin Order Placement (Order Type 2: SYSTEM)
    @Transactional
    @CacheEvict(value = {"saleOrders", "saleOrder", "saleOrderReportList", "saleOrderReportSummary"}, allEntries = true)
    public void createSystemOrder(SaleOrderRequest request, Long userId) {
        saleOrderValidator.validateOrderRequest(request, null);

        Customer customer = request.getCustomerId() != null
                ? customerService.getCustomerById(request.getCustomerId())
                : customerService.getOrCreateCustomer(request.getPassengerName(), request.getPassengerPhone(), null, (byte) 1);

        SaleOrder order = saleOrderMapper.fromRequest(request);
        order.setCustomerId(customer.getId());
        order.setOrderType((byte) 2);
        order.setOrderNo(saleOrderRepository.generateOrderNo());
        order.setOrderDate(LocalDateTime.now());
        order.setCreated(LocalDateTime.now());
        order.setCreatedBy(userId);

        // * Auto-calculate payment breakdown for System Orders
        saleOrderValidator.autoCalculateOrderPayment(order, request);

        List<SaleOrderTrip> trips = saleOrderValidator.processTripRequests(request.getTrips());
        saleOrderRepository.saveOrder(order, trips);
    }

    // * Mobile Customer Tickets Listing
    @CircuitBreaker(name = "defaultService")
    public PageResult<SaleOrderResponse> getMobileCustomerTickets(String rawToken, SaleOrderFilterRequest req) {
        AuthUserResponse authUser = authWebhookClient.verifyAndGetUserInfo(rawToken)
                .orElseThrow(() -> new GlobalException("Invalid or unverified user token", 401));

        Long userId = parseUserId(authUser.getUserId());
        Long customerId = customerService.findExistingCustomerId(userId, authUser.getPhone(), authUser.getEmail());

        SaleOrderFilterRequest filter = req != null ? req : new SaleOrderFilterRequest();
        filter.setCustomerId(customerId != null ? customerId : (userId != null ? userId : -1L));
        filter.setCreatedBy(userId);

        long total = QueryUtil.shouldCount(filter.getPage(), () -> saleOrderRepository.countAll(filter));
        return new PageResult<>(saleOrderMapper.toResponses(saleOrderRepository.findAll(filter)), total);
    }

    // * Admin update sale order details & trip itineraries
    @Transactional
    @CacheEvict(value = {"saleOrders", "saleOrder", "saleOrderReportList", "saleOrderReportSummary"}, allEntries = true)
    public void updateOrder(Long id, SaleOrderRequest request, Long userId) {
        SaleOrder order = getOrderById(id);
        saleOrderValidator.validateEditableOrder(order, "update");
        saleOrderValidator.validateOrderRequest(request, id);

        BigDecimal oldTotal = order.getTotalAmount();
        saleOrderMapper.updateFromRequest(request, order);
        saleOrderValidator.autoCalculateOrderPayment(order, request);
        order.setModifiedBy(userId);
        saleOrderRepository.updateOrder(order, saleOrderValidator.processTripRequests(request.getTrips()), oldTotal, userId);
    }

    // * Update order status & optional void remark
    @Transactional
    @CacheEvict(value = {"saleOrders", "saleOrder", "saleOrderReportList", "saleOrderReportSummary"}, allEntries = true)
    public void updateOrderStatus(Long id, Byte status, String voidRemark, Long userId) {
        getOrderById(id);
        saleOrderRepository.updateOrderStatus(id, status, voidRemark, userId);
    }

    // * Void sale order
    @Transactional
    @CacheEvict(value = {"saleOrders", "saleOrder", "saleOrderReportList", "saleOrderReportSummary"}, allEntries = true)
    public void voidOrder(Long id, String voidRemark, Long userId) {
        saleOrderValidator.validateEditableOrder(getOrderById(id), "void");
        saleOrderRepository.voidOrder(id, voidRemark, userId);
    }

    // * Soft delete sale order
    @Transactional
    @CacheEvict(value = {"saleOrders", "saleOrder", "saleOrderReportList", "saleOrderReportSummary"}, allEntries = true)
    public void deleteOrder(Long id, Long userId) {
        saleOrderValidator.validateEditableOrder(getOrderById(id), "delete");
        saleOrderRepository.deleteById(id, userId);
    }

    // * Update payment receipt, discount adjustments, and paid balances
    @Transactional
    @CacheEvict(value = {"saleOrders", "saleOrder", "saleOrderReportList", "saleOrderReportSummary"}, allEntries = true)
    public void updateReceipt(Long id, ReceiptUpdateRequest req, Long userId) {
        SaleOrder order = getOrderById(id);
        saleOrderValidator.validatePayableOrder(order, "update receipt for");

        BigDecimal oldTotal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal oldDiscount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal effectiveDiscount = req.getDiscountAmount() != null ? req.getDiscountAmount() : oldDiscount;

        BigDecimal sub = order.getSubtotalAmount() != null ? order.getSubtotalAmount() : BigDecimal.ZERO;
        BigDecimal fee = order.getServiceFee() != null ? order.getServiceFee() : BigDecimal.ZERO;
        BigDecimal tax = order.getTaxAmount() != null ? order.getTaxAmount() : BigDecimal.ZERO;

        BigDecimal total = req.getDiscountAmount() != null
                ? (sub.compareTo(BigDecimal.ZERO) > 0 || fee.compareTo(BigDecimal.ZERO) > 0 || tax.compareTo(BigDecimal.ZERO) > 0
                ? sub.add(fee).add(tax).subtract(effectiveDiscount).max(BigDecimal.ZERO)
                : oldTotal.add(oldDiscount).subtract(effectiveDiscount).max(BigDecimal.ZERO))
                : oldTotal;

        BigDecimal existingPaid = order.getPaidAmount() != null ? order.getPaidAmount() : BigDecimal.ZERO;
        Byte pType = req.getPaymentType() != null ? req.getPaymentType() : (order.getPaymentType() != null ? order.getPaymentType() : (byte) 1);
        BigDecimal effectiveDeposit = req.getDepositPrice() != null && req.getDepositPrice().compareTo(BigDecimal.ZERO) > 0
                ? req.getDepositPrice()
                : (order.getDepositPrice() != null ? order.getDepositPrice() : BigDecimal.ZERO);

        // * Calculate payment step amount & cumulative paid balance
        BigDecimal stepAmount;
        BigDecimal paid;
        if (req.getPaidAmount() != null && req.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            stepAmount = req.getPaidAmount();
            paid = existingPaid.add(stepAmount).min(total);
        } else if (pType == 4) {
            stepAmount = effectiveDeposit.compareTo(BigDecimal.ZERO) > 0 ? effectiveDeposit : total.subtract(existingPaid).max(BigDecimal.ZERO);
            paid = existingPaid.add(stepAmount).min(total);
        } else if (req.getDepositPrice() != null && req.getDepositPrice().compareTo(BigDecimal.ZERO) > 0) {
            stepAmount = req.getDepositPrice();
            paid = existingPaid.add(stepAmount).min(total);
        } else if (req.getDiscountAmount() != null && req.getReceiptFileName() == null && req.getReceiptFileUrl() == null && req.getPaymentMethod() == null) {
            stepAmount = BigDecimal.ZERO;
            paid = existingPaid.min(total);
        } else {
            stepAmount = total.subtract(existingPaid).max(BigDecimal.ZERO);
            paid = total;
        }

        BigDecimal remaining = total.subtract(paid).max(BigDecimal.ZERO);

        // * Enforce payment status: 3 = Paid, 2 = Deposit, 1 = Booking
        Byte status = req.getPaymentStatus();
        if (total.compareTo(BigDecimal.ZERO) > 0 && paid.compareTo(total) >= 0) {
            status = (byte) 3;
        } else if (paid.compareTo(BigDecimal.ZERO) > 0 || pType == 4) {
            status = (byte) 2;
        } else if (status == null) {
            status = order.getPaymentStatus() != null ? order.getPaymentStatus() : (byte) 1;
        }

        Integer method = req.getPaymentMethod() != null ? req.getPaymentMethod() : order.getPaymentMethod();

        saleOrderRepository.updateReceipt(id, pType, status, method, paid, effectiveDeposit, effectiveDiscount, total, remaining,
                req.getReceiptFileName(), req.getReceiptFileUrl(), req.getReceiptDescription(), stepAmount, oldTotal, userId);
    }

    // * Replace trips for an order & recalculate order financial totals
    @Transactional
    @CacheEvict(value = {"saleOrders", "saleOrder", "saleOrderReportList", "saleOrderReportSummary"}, allEntries = true)
    public void updateTrips(Long id, List<SaleOrderTripRequest> tripRequests, Long userId) {
        SaleOrder order = getOrderById(id);
        saleOrderValidator.validateEditableOrder(order, "update trips for");

        List<SaleOrderTrip> trips = saleOrderValidator.processTripRequests(tripRequests);
        BigDecimal oldTotal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;

        BigDecimal newSubtotal   = saleOrderValidator.calculateTripsBaseSubtotal(trips);
        BigDecimal newServiceFee = saleOrderValidator.calculateTripsServiceFee(trips);
        BigDecimal tax           = order.getTaxAmount() != null ? order.getTaxAmount() : BigDecimal.ZERO;
        BigDecimal discount      = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal newTotal      = newSubtotal.add(newServiceFee).add(tax).subtract(discount).max(BigDecimal.ZERO);

        BigDecimal paid        = order.getPaidAmount() != null ? order.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal newRemaining = newTotal.subtract(paid).max(BigDecimal.ZERO);

        Byte newPaymentStatus = (newTotal.compareTo(BigDecimal.ZERO) > 0 && paid.compareTo(newTotal) >= 0)
                ? (byte) 3
                : (paid.compareTo(BigDecimal.ZERO) > 0 ? (byte) 2 : (byte) 1);

        saleOrderRepository.updateTripsAndOrderFinancials(id, trips, newSubtotal, newServiceFee, newTotal, newRemaining, newPaymentStatus, oldTotal, userId);
    }

    private Long parseUserId(String val) {
        if (val == null || val.isBlank()) return null;
        try { return Long.parseLong(val.trim()); } catch (Exception e) { return null; }
    }
}
