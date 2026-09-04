package com.Car_Rental_API.module.mobile.controller;


import com.Car_Rental_API.module.report.customer_review.service.CustomerReviewService;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Car_Rental_API.common.base.BaseController;
import com.Car_Rental_API.common.base_dto.response.BaseResponse;

import com.Car_Rental_API.module.report.customer_review.dto.request.CustomerReviewRequest;
import com.Car_Rental_API.module.sale_order.service.SaleOrderService;
import com.Car_Rental_API.module.sale_order.dto.response.MobileBookingCreateResponse;
import com.Car_Rental_API.module.sale_order.dto.request.SaleOrderFilterRequest;
import com.Car_Rental_API.module.sale_order.dto.request.SaleOrderRequest;
import com.Car_Rental_API.module.sale_order.dto.response.SaleOrderResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Tag(name = "26. Customer Bookings & Tickets", description = "APIs for Customer Order Placement, Ticket History & Submitting Reviews")
public class MobileBookingController extends BaseController {

	private final SaleOrderService saleOrderService;
	private final CustomerReviewService customerReviewService;

	// * Place Customer Booking Order
	@PostMapping
	@Operation(summary = "Place customer booking order (Verifies token & auto-links customer)")
	public ResponseEntity<BaseResponse<MobileBookingCreateResponse>> createMobileOrder(HttpServletRequest servletRequest, @Valid @RequestBody SaleOrderRequest request) {
		return success(saleOrderService.createAppOrder(extractToken(servletRequest), request));
	}

	// * Get Customer Tickets List
	@GetMapping
	@Operation(summary = "Get list of customer ticket orders using Token", description = "orderStatus: 1 = To Do, 2 = In Progress, 3 = Complete, 4 = Void | paymentStatus: 1 = Pending, 2 = Paid, 3 = Expired/Cancelled")
	public ResponseEntity<BaseResponse<List<SaleOrderResponse>>> getMobileCustomerTickets(HttpServletRequest servletRequest, @Valid SaleOrderFilterRequest request) {
		return successPage(saleOrderService.getMobileCustomerTickets(extractToken(servletRequest), request), request);
	}

	// * Get Specific Ticket Details
	@GetMapping("/{id}")
	@Operation(summary = "Get specific ticket order details")
	public ResponseEntity<BaseResponse<SaleOrderResponse>> getMobileTicketDetails(@PathVariable Long id) {
		return success(saleOrderService.getOrderResponseById(id));
	}

	// * Submit Customer Review
	@PostMapping("/reviews")
	@Operation(summary = "Submit a customer review for completed booking")
	public ResponseEntity<BaseResponse<Void>> submitReview(HttpServletRequest servletRequest, @Valid @RequestBody CustomerReviewRequest request) {
		return successVoid(() -> customerReviewService.submitFromApp(extractToken(servletRequest), request));
	}

	private String extractToken(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		return authHeader != null ? authHeader : "";
	}
}

