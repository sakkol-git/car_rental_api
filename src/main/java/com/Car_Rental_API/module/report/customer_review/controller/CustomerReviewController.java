package com.Car_Rental_API.module.report.customer_review.controller;



import com.Car_Rental_API.module.report.customer_review.dto.response.CustomerReviewResponse;
import com.Car_Rental_API.module.report.customer_review.service.CustomerReviewService;
import com.Car_Rental_API.common.base.BaseController;
import com.Car_Rental_API.common.base_dto.response.BaseResponse;
import com.Car_Rental_API.module.report.customer_review.dto.request.CustomerReviewFilterRequest;

import com.Car_Rental_API.security.authorization.util.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer-reviews")
@RequiredArgsConstructor
@Tag(name = "22. Customer Review", description = "Admin: list customer reviews, toggle app visibility")
public class CustomerReviewController extends BaseController {

    private final CustomerReviewService customerReviewService;

    @PostMapping
    @RequiresPermission(module = "Customer Review", action = "View")
    @Operation(summary = "List customer reviews with filters (dateFrom, dateTo, keyword, vehicleId, customerId, salesOrderId)")
    public ResponseEntity<BaseResponse<List<CustomerReviewResponse>>> getAll(@RequestBody(required = false) CustomerReviewFilterRequest req) {
        CustomerReviewFilterRequest filter = req != null ? req : new CustomerReviewFilterRequest();
        return successPage(customerReviewService.getAll(filter), filter);
    }

    @GetMapping("/{id}")
    @RequiresPermission(module = "Customer Review", action = "View")
    @Operation(summary = "Get customer review by ID")
    public ResponseEntity<BaseResponse<CustomerReviewResponse>> getById(@PathVariable Long id) {
        return success(customerReviewService.getById(id));
    }

    // * Toggle disable/enable review visibility on mobile app
    @PatchMapping("/{id}/disable")
    @RequiresPermission(module = "Customer Review", action = "Public To App")
    @Operation(summary = "Toggle review visibility on app (0: enabled, 1: disabled)")
    public ResponseEntity<BaseResponse<Void>> setDisabled(@PathVariable Long id, @RequestParam Byte isDisabled) {
        return successVoid(() -> customerReviewService.setDisabled(id, isDisabled));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission(module = "Customer Review", action = "Delete")
    @Operation(summary = "Soft-delete a customer review")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id) {
        return successVoid(() -> customerReviewService.delete(id));
    }
}
