package com.Car_Rental_API.module.sale_order.mapper;

import com.Car_Rental_API.module.sale_order.model.*;
import com.Car_Rental_API.module.sale_order.dto.response.*;

import com.Car_Rental_API.module.sale_order.dto.request.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SaleOrderMapper {

    @Mapping(target = "createdBy", source = "createdByFullName")
    @Mapping(target = "modifiedBy", source = "modifiedByFullName")
    SaleOrderResponse toResponse(SaleOrder order);

    @AfterMapping
    default void handleCustomerAndAddressFallback(@MappingTarget SaleOrderResponse res, SaleOrder order) {
        if (res.getCustomerName() == null || res.getCustomerName().isBlank()) {
            res.setCustomerName(order.getPassengerName());
        }
        if (res.getCustomerPhone() == null || res.getCustomerPhone().isBlank()) {
            res.setCustomerPhone(order.getPassengerPhone());
        }
        if (order.getTrips() != null && !order.getTrips().isEmpty()) {
            if (res.getPickupAddress() == null || res.getPickupAddress().isBlank()) {
                SaleOrderTrip firstTrip = order.getTrips().get(0);
                if (firstTrip.getFromProvinceName() != null && !firstTrip.getFromProvinceName().isBlank()) {
                    res.setPickupAddress(firstTrip.getFromProvinceName());
                }
            }
            if (res.getDropoffAddress() == null || res.getDropoffAddress().isBlank()) {
                SaleOrderTrip lastTrip = order.getTrips().get(order.getTrips().size() - 1);
                if (lastTrip.getToProvinceName() != null && !lastTrip.getToProvinceName().isBlank()) {
                    res.setDropoffAddress(lastTrip.getToProvinceName());
                }
            }
        }
    }

    List<SaleOrderResponse> toResponses(List<SaleOrder> orders);

    SaleOrderTripResponse toTripResponse(SaleOrderTrip trip);
    List<SaleOrderTripResponse> toTripResponses(List<SaleOrderTrip> trips);

    SaleOrderPaymentHistoryResponse toHistoryResponse(SaleOrderPaymentHistory history);
    List<SaleOrderPaymentHistoryResponse> toHistoryResponses(List<SaleOrderPaymentHistory> histories);

    @Mapping(target = "isActive", constant = "1")
    @Mapping(target = "orderStatus", constant = "1")
    SaleOrder fromRequest(SaleOrderRequest request);

    @Mapping(target = "isActive", constant = "1")
    @Mapping(target = "status", expression = "java(req.getStatus() != null ? req.getStatus() : 3)")
    SaleOrderTrip fromTripRequest(SaleOrderTripRequest req);

    List<SaleOrderTrip> fromTripRequests(List<SaleOrderTripRequest> requests);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(SaleOrderRequest request, @MappingTarget SaleOrder order);
}
