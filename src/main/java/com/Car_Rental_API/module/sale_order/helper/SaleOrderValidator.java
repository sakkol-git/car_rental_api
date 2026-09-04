package com.Car_Rental_API.module.sale_order.helper;

import com.Car_Rental_API.module.master_data.vechicle.model.Vehicle;
import com.Car_Rental_API.common.exception.GlobalException;
import com.Car_Rental_API.module.sale_order.model.TripSubLocationItem;
import com.Car_Rental_API.module.sale_order.dto.request.TripSubLocationRequest;
import com.Car_Rental_API.common.util.LocationUtil;







import com.Car_Rental_API.module.sale_order.model.SaleOrderTrip;
import com.Car_Rental_API.module.sale_order.dto.request.SaleOrderTripRequest;
import com.Car_Rental_API.module.sale_order.model.SaleOrder;
import com.Car_Rental_API.module.sale_order.dto.request.SaleOrderRequest;
import com.Car_Rental_API.module.sale_order.repository.SaleOrderRepository;
import com.Car_Rental_API.module.master_data.vechicle.repository.VehicleRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;


import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SaleOrderValidator {

    private final VehicleRepository vehicleRepository;
    private final SaleOrderRepository saleOrderRepository;

    // * Validate Order Inputs, Location Addresses, & Vehicle Quantity Availability
    public void validateOrderRequest(SaleOrderRequest request, Long excludeOrderId) {
        if (request == null || request.getVehicleId() == null || request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Vehicle ID, Start date, and End date are required.");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after End date.");
        }

        // * Auto-extract lat/lng from raw location strings and truncate coordinates from address
        cleanAndExtractLocationCoordinates(request);

        // * Validate pickup & dropoff address location inputs
        // boolean hasPickup = request.getPickupAddress() != null && !request.getPickupAddress().isBlank();
        // boolean hasDropoff = request.getDropoffAddress() != null && !request.getDropoffAddress().isBlank();
        // if (!hasPickup || !hasDropoff) {
        //     throw new IllegalArgumentException("Pickup and dropoff addresses are required.");
        // }

        // * Validate vehicle master setting quantity & peak daily booked count
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Selected vehicle not found or inactive."));

        int totalLimit = vehicle.getQuantity() != null && vehicle.getQuantity() > 0 ? vehicle.getQuantity() : 1;
        int requestedQty = request.getAmountOfVehicles() != null && request.getAmountOfVehicles() > 0 ? request.getAmountOfVehicles() : 1;
        int maxBooked = saleOrderRepository.getMaxBookedVehiclesInDateRange(request.getVehicleId(), request.getStartDate(), request.getEndDate(), excludeOrderId);
        int availableCount = totalLimit - maxBooked;

        if (requestedQty > availableCount) {
            throw new IllegalStateException("Selected vehicle '" + vehicle.getNameEn() + "' is fully booked for date range ("
                    + request.getStartDate() + " to " + request.getEndDate() + "). Available: "
                    + Math.max(0, availableCount) + ", Requested: " + requestedQty + ".");
        }
    }

    // * Check if an order can be edited or deleted (Blocked for Complete = 3, Void = 4, Rejected = 5)
    public void validateEditableOrder(SaleOrder order, String action) {
        if (order != null && order.getOrderStatus() != null) {
            byte status = order.getOrderStatus();
            if (status == 3 || status == 4 || status == 5) {
                String label = status == 3 ? "Complete" : (status == 4 ? "Void" : "Rejected");
                throw new GlobalException("Cannot " + action + " order with status: " + label, 400);
            }
        }
    }

    // * Check if an order can receive payment receipts (Blocked for Void = 4, Rejected = 5)
    public void validatePayableOrder(SaleOrder order, String action) {
        if (order != null && order.getOrderStatus() != null) {
            byte status = order.getOrderStatus();
            if (status == 4 || status == 5) {
                String label = status == 4 ? "Void" : "Rejected";
                throw new GlobalException("Cannot " + action + " order with status: " + label, 400);
            }
        }
    }

    // * Process and map trip requests to domain model objects
    public List<SaleOrderTrip> processTripRequests(List<SaleOrderTripRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return new ArrayList<>();
        }
        List<SaleOrderTrip> result = new ArrayList<>();
        int orderIndex = 1;
        for (SaleOrderTripRequest req : requests) {
            if (req == null) continue;

            SaleOrderTrip trip = new SaleOrderTrip();
            trip.setFromProvinceId(req.getFromProvinceId());
            trip.setToProvinceId(req.getToProvinceId());
            trip.setPrice(req.getPrice());
            trip.setStatus(req.getStatus() != null ? req.getStatus() : 3);
            trip.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : orderIndex++);
            trip.setIsActive(1);

            List<TripSubLocationItem> subItems = new ArrayList<>();
            if (req.getSubLocations() != null && !req.getSubLocations().isEmpty()) {
                int subIndex = 1;
                for (TripSubLocationRequest item : req.getSubLocations()) {
                    if (item == null || item.getSubLocationId() == null) continue;
                    subItems.add(TripSubLocationItem.builder()
                            .subLocationId(item.getSubLocationId())
                            .price(item.getPrice())
                            .sortOrder(item.getSortOrder() != null ? item.getSortOrder() : subIndex++)
                            .build());
                }
            }
            trip.setSubLocations(subItems);
            result.add(trip);
        }
        return result;
    }

    // * Calculate base subtotal: sum of trip.price only (from→to route base price per leg)
    public BigDecimal calculateTripsBaseSubtotal(List<SaleOrderTrip> trips) {
        if (trips == null || trips.isEmpty()) return BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        for (SaleOrderTrip trip : trips) {
            if (trip != null && trip.getPrice() != null) {
                total = total.add(trip.getPrice());
            }
        }
        return total;
    }

    // * Calculate service fee: sum of all sub-location prices across all trips
    public BigDecimal calculateTripsServiceFee(List<SaleOrderTrip> trips) {
        if (trips == null || trips.isEmpty()) return BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        for (SaleOrderTrip trip : trips) {
            if (trip == null || trip.getSubLocations() == null) continue;
            for (TripSubLocationItem item : trip.getSubLocations()) {
                if (item != null && item.getPrice() != null) {
                    total = total.add(item.getPrice());
                }
            }
        }
        return total;
    }

    // * Auto-clean raw location strings, extract lat/lng coordinates, and extract Province name
    public void cleanAndExtractLocationCoordinates(SaleOrderRequest request) {
        if (request == null) return;

        if (request.getPickupAddress() != null && !request.getPickupAddress().isBlank()) {
            LocationUtil.LocationCoordinates c = LocationUtil.extractCoordinates(request.getPickupAddress());
            if (request.getPickupLatitude() == null) request.setPickupLatitude(c.getLatitude());
            if (request.getPickupLongitude() == null) request.setPickupLongitude(c.getLongitude());
            request.setPickupAddress(LocationUtil.extractProvinceOnly(request.getPickupAddress()));
        }

        if (request.getDropoffAddress() != null && !request.getDropoffAddress().isBlank()) {
            LocationUtil.LocationCoordinates c = LocationUtil.extractCoordinates(request.getDropoffAddress());
            if (request.getDropoffLatitude() == null) request.setDropoffLatitude(c.getLatitude());
            if (request.getDropoffLongitude() == null) request.setDropoffLongitude(c.getLongitude());
            request.setDropoffAddress(LocationUtil.extractProvinceOnly(request.getDropoffAddress()));
        }
    }

    // * Auto-calculate payment breakdown & payment status for sale orders (create & update)
    public void autoCalculateOrderPayment(SaleOrder order, SaleOrderRequest request) {
        // 1. Calculate base subtotal and service fee from trips
        BigDecimal tripsSubtotal = BigDecimal.ZERO;
        BigDecimal tripsServiceFee = BigDecimal.ZERO;

        if (request != null && request.getTrips() != null && !request.getTrips().isEmpty()) {
            for (var tripReq : request.getTrips()) {
                if (tripReq == null) continue;
                if (tripReq.getPrice() != null) {
                    tripsSubtotal = tripsSubtotal.add(tripReq.getPrice());
                }
                if (tripReq.getSubLocations() != null) {
                    for (var sub2 : tripReq.getSubLocations()) {
                        if (sub2 != null && sub2.getPrice() != null) {
                            tripsServiceFee = tripsServiceFee.add(sub2.getPrice());
                        }
                    }
                }
            }
        }

        // Subtotal: prefer trips calculation if trips exist, else request subtotal, else keep existing
        BigDecimal subtotal = tripsSubtotal.compareTo(BigDecimal.ZERO) > 0
                ? tripsSubtotal
                : (request != null && request.getSubtotalAmount() != null && request.getSubtotalAmount().compareTo(BigDecimal.ZERO) > 0
                ? request.getSubtotalAmount()
                : (order.getSubtotalAmount() != null ? order.getSubtotalAmount() : BigDecimal.ZERO));
        order.setSubtotalAmount(subtotal);

        // Service Fee: prefer trips calculation if trips exist, else request service fee, else keep existing
        BigDecimal serviceFee = tripsServiceFee.compareTo(BigDecimal.ZERO) > 0
                ? tripsServiceFee
                : (request != null && request.getServiceFee() != null && request.getServiceFee().compareTo(BigDecimal.ZERO) > 0
                ? request.getServiceFee()
                : (order.getServiceFee() != null ? order.getServiceFee() : BigDecimal.ZERO));
        order.setServiceFee(serviceFee);

        // Discount
        BigDecimal discount = (request != null && request.getDiscountAmount() != null)
                ? request.getDiscountAmount()
                : (order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO);
        order.setDiscountAmount(discount);

        // Tax
        BigDecimal tax = (request != null && request.getTaxAmount() != null)
                ? request.getTaxAmount()
                : (order.getTaxAmount() != null ? order.getTaxAmount() : BigDecimal.ZERO);
        order.setTaxAmount(tax);

        // Total Amount: subtotal + serviceFee + tax - discount
        BigDecimal totalAmount;
        if (subtotal.compareTo(BigDecimal.ZERO) > 0 || serviceFee.compareTo(BigDecimal.ZERO) > 0 || tax.compareTo(BigDecimal.ZERO) > 0) {
            totalAmount = subtotal.add(serviceFee).add(tax).subtract(discount).max(BigDecimal.ZERO);
        } else if (request != null && request.getTotalAmount() != null && request.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            totalAmount = request.getTotalAmount();
        } else if (order.getTotalAmount() != null && order.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            totalAmount = order.getTotalAmount();
        } else {
            totalAmount = BigDecimal.ZERO;
        }
        order.setTotalAmount(totalAmount);

        // Deposit Price
        BigDecimal depositPrice = (request != null && request.getDepositPrice() != null)
                ? request.getDepositPrice()
                : (order.getDepositPrice() != null ? order.getDepositPrice() : BigDecimal.ZERO);
        order.setDepositPrice(depositPrice);

        // Paid Amount:
        BigDecimal paidAmount;
        if (request != null && request.getPaidAmount() != null && request.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            paidAmount = request.getPaidAmount();
        } else if (request != null && request.getPaymentStatus() != null && request.getPaymentStatus() == 2 && depositPrice.compareTo(BigDecimal.ZERO) > 0) {
            paidAmount = depositPrice;
        } else if (request != null && request.getPaymentStatus() != null && request.getPaymentStatus() == 3) {
            paidAmount = totalAmount;
        } else if (request != null && (request.getReceiptFileUrl() != null || request.getReceiptFileName() != null)) {
            paidAmount = depositPrice.compareTo(BigDecimal.ZERO) > 0 ? depositPrice : totalAmount;
        } else if (order.getPaidAmount() != null && order.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            paidAmount = order.getPaidAmount();
        } else if (depositPrice.compareTo(BigDecimal.ZERO) > 0 && order.getPaymentType() != null && order.getPaymentType() == 4) {
            paidAmount = depositPrice;
        } else {
            paidAmount = BigDecimal.ZERO;
        }
        order.setPaidAmount(paidAmount);

        // Remaining Amount: totalAmount - paidAmount (clamped to >= 0)
        BigDecimal remainingAmount = totalAmount.subtract(paidAmount).max(BigDecimal.ZERO);
        order.setRemainingAmount(remainingAmount);

        // Vehicle Category & Rental Type defaults
        if (order.getVehicleCategoryId() == null) {
            order.setVehicleCategoryId((request != null && request.getVehicleCategoryId() != null) ? request.getVehicleCategoryId() : 1L);
        }
        if (order.getVehicleRentalTypeId() == null) {
            order.setVehicleRentalTypeId((request != null && request.getVehicleRentalTypeId() != null) ? request.getVehicleRentalTypeId() : 1L);
        }
        if (order.getPaymentMethod() == null) {
            order.setPaymentMethod(request != null && request.getPaymentMethod() != null ? request.getPaymentMethod() : 1);
        }

        // Payment Type: 1 = Bank Paid, 2 = Cash on Hand, 3 = KHQR, 4 = Deposit
        if (request != null && request.getPaymentType() != null) {
            order.setPaymentType(request.getPaymentType());
        } else if (order.getPaymentType() == null || order.getPaymentType() == 1) {
            if (depositPrice.compareTo(BigDecimal.ZERO) > 0 && (paidAmount.compareTo(totalAmount) < 0 || (request != null && request.getPaymentStatus() != null && request.getPaymentStatus() == 2))) {
                order.setPaymentType((byte) 4); // 4: Deposit
            } else if (paidAmount.compareTo(totalAmount) >= 0 && totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                order.setPaymentType((byte) 3); // 3: KHQR / Bank Paid
            } else if (order.getPaymentType() == null) {
                order.setPaymentType((byte) 1); // 1: Bank Paid
            }
        }

        // Auto-calculate / enforce correct paymentStatus:
        // 3 = Paid (when total > 0 and paid >= total)
        // 2 = Deposit (when paid > 0, or depositPrice > 0 with status 2 or type 4)
        // 4 = Expired / Cancelled (if request explicitly says 4)
        // 1 = Booking (when paid == 0)
        if (request != null && request.getPaymentStatus() != null && request.getPaymentStatus() == 4) {
            order.setPaymentStatus((byte) 4); // 4: Expired / Cancelled
        } else if (totalAmount.compareTo(BigDecimal.ZERO) > 0 && paidAmount.compareTo(totalAmount) >= 0) {
            order.setPaymentStatus((byte) 3); // 3: Paid
        } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0 || (request != null && request.getPaymentStatus() != null && request.getPaymentStatus() == 2)) {
            order.setPaymentStatus((byte) 2); // 2: Deposit / Partial
        } else {
            order.setPaymentStatus((byte) 1); // 1: Booking
        }

        // Auto-calculate orderStatus:
        // 1: To Do, 2: In Progress, 3: Complete, 4: Void
        if (request != null && request.getPaymentStatus() != null && request.getPaymentStatus() == 4) {
            order.setOrderStatus((byte) 4); // Void
        } else if (order.getOrderStatus() == null || order.getOrderStatus() == 1 || order.getOrderStatus() == 2) {
            LocalDate today = LocalDate.now();
            LocalDate start = order.getStartDate();
            LocalDate end = order.getEndDate() != null ? order.getEndDate() : start;

            if (end != null && end.isBefore(today)) {
                order.setOrderStatus((byte) 3); // 3: Complete
            } else if (start != null && !start.isAfter(today)) {
                order.setOrderStatus((byte) 2); // 2: In Progress
            } else {
                order.setOrderStatus((byte) 1); // 1: To Do
            }
        }
    }
}
