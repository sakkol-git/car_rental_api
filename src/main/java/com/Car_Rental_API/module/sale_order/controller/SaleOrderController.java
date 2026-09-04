package com.Car_Rental_API.module.sale_order.controller;








import com.Car_Rental_API.module.sale_order.dto.request.SaleOrderTripRequest;
import com.Car_Rental_API.module.sale_order.dto.request.ReceiptUpdateRequest;
import com.Car_Rental_API.module.sale_order.dto.request.SaleOrderVoidRequest;
import com.Car_Rental_API.module.sale_order.dto.request.SaleOrderRequest;
import com.Car_Rental_API.module.sale_order.dto.response.SaleOrderResponse;
import com.Car_Rental_API.module.sale_order.dto.request.SaleOrderFilterRequest;
import java.util.List;

import com.Car_Rental_API.common.base.BaseController;
import com.Car_Rental_API.common.base_dto.response.BaseResponse;
import com.Car_Rental_API.module.sale_order.service.SaleOrderService;
import com.Car_Rental_API.security.authorization.util.RequiresPermission;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// * Controller mapping for Admin Sale Order Management
@RestController
@RequestMapping("/sale-orders")
@RequiredArgsConstructor
@Tag(name = "21. Sale Order", description = "Admin Sale Order Management APIs")
public class SaleOrderController extends BaseController {

    private final SaleOrderService saleOrderService;

    // * Search sale orders list with pagination & filters
    @PostMapping("/list")
    @RequiresPermission(module = "Sale Order", action = "View")
    @Operation(summary = "Get all sale orders with filter via POST body", description = "orderStatus: 1 = To Do, 2 = In Progress, 3 = Complete, 4 = Void")
    public ResponseEntity<BaseResponse<List<SaleOrderResponse>>> getAllOrdersPost(@RequestBody(required = false) SaleOrderFilterRequest req) {
        SaleOrderFilterRequest filter = req != null ? req : new SaleOrderFilterRequest();
        return successPage(saleOrderService.getAllOrders(filter), filter);
    }

    // * Get order details by ID
    @GetMapping("/{id}")
    @RequiresPermission(module = "Sale Order", action = "View")
    @Operation(summary = "Get sale order by ID")
    public ResponseEntity<BaseResponse<SaleOrderResponse>> getOrderById(@PathVariable Long id) {
        return success(saleOrderService.getOrderResponseById(id));
    }

    // * Create manual system sale order
    @PostMapping
    @RequiresPermission(module = "Sale Order", action = "Add")
    @Operation(summary = "Create manual sale order by System Admin")
    public ResponseEntity<BaseResponse<Void>> createSystemOrder(@Valid @RequestBody SaleOrderRequest req) {
        return successVoid(userId -> saleOrderService.createSystemOrder(req, userId));
    }

    // * Update full sale order details
    @PutMapping("/{id}")
    @RequiresPermission(module = "Sale Order", action = "Edit")
    @Operation(summary = "Update full sale order (all fields + trips)")
    public ResponseEntity<BaseResponse<Void>> updateOrder(@PathVariable Long id, @Valid @RequestBody SaleOrderRequest req) {
        return successVoid(userId -> saleOrderService.updateOrder(id, req, userId));
    }

    // * Update order status: 1=To Do, 2=In Progress, 3=Complete, 4=Void
    @PatchMapping("/{id}/status")
    @RequiresPermission(module = "Sale Order", action = "Update Status")
    @Operation(summary = "Update order status", description = "orderStatus: 1 = To Do, 2 = In Progress, 3 = Complete, 4 = Void")
    public ResponseEntity<BaseResponse<Void>> updateOrderStatus(@PathVariable Long id, @RequestParam Byte status, @RequestParam(required = false) String remark) {
        return successVoid(userId -> saleOrderService.updateOrderStatus(id, status, remark, userId));
    }

    // * Void sale order with optional remark
    @PatchMapping("/{id}/void")
    @RequiresPermission(module = "Sale Order", action = "Void")
    @Operation(summary = "Void sale order with optional remark", description = "Sets orderStatus to 4 (Void) and records optional voidRemark.")
    public ResponseEntity<BaseResponse<Void>> voidOrder(@PathVariable Long id, @RequestBody(required = false) SaleOrderVoidRequest req, @RequestParam(required = false) String remark) {
        String r = (req != null && req.getRemark() != null && !req.getRemark().isBlank()) ? req.getRemark() : remark;
        return successVoid(userId -> saleOrderService.voidOrder(id, r, userId));
    }

    // * Upload / update payment receipt & payment status
    @PatchMapping("/{id}/receipt")
    @RequiresPermission(module = "Sale Order", action = "Pay Receipt")
    @Operation(summary = "Upload / update payment receipt and payment type")
    public ResponseEntity<BaseResponse<Void>> updateReceipt(@PathVariable Long id, @Valid @RequestBody ReceiptUpdateRequest req) {
        return successVoid(userId -> saleOrderService.updateReceipt(id, req, userId));
    }

    // * Update trip itinerary for an order
    @PutMapping("/{id}/trips")
    @RequiresPermission(module = "Sale Order", action = "Update Trips")
    @Operation(summary = "Update trip itinerary for an order")
    public ResponseEntity<BaseResponse<Void>> updateTrips(@PathVariable Long id, @Valid @RequestBody List<@Valid SaleOrderTripRequest> trips) {
        return successVoid(userId -> saleOrderService.updateTrips(id, trips, userId));
    }

    // * Soft-delete sale order
    @DeleteMapping("/{id}")
    @RequiresPermission(module = "Sale Order", action = "Delete")
    @Operation(summary = "Soft-delete sale order")
    public ResponseEntity<BaseResponse<Void>> deleteOrder(@PathVariable Long id) {
        return successVoid(userId -> saleOrderService.deleteOrder(id, userId));
    }
}
