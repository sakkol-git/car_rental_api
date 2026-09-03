package com.Car_Rental_API.module.master_data.customer_support.controller;

import com.Car_Rental_API.module.master_data.customer_support.repository.*;
import com.Car_Rental_API.module.master_data.customer_support.mapper.*;
import com.Car_Rental_API.module.master_data.customer_support.service.*;
import com.Car_Rental_API.module.master_data.customer_support.model.*;
import com.Car_Rental_API.module.master_data.customer_support.dto.*;


import com.Car_Rental_API.common.base.BaseController;
import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.BaseResponse;
import com.Car_Rental_API.module.master_data.customer_support.dto.CustomerSupportRequest;
import com.Car_Rental_API.module.master_data.customer_support.dto.CustomerSupportResponse;
import com.Car_Rental_API.security.authorization.util.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/masterdata/customer-support")
@RequiredArgsConstructor
@Tag(name = "17. Master Data - Customer Support", description = "APIs for Customer Support Management")
public class CustomerSupportController extends BaseController {

    private final CustomerSupportService customerSupportService;

    @GetMapping
    @RequiresPermission(module = "Customer Support", action = "View")
    @Operation(summary = "1: Telegram, 2: Facebook, 3: Phone, 4: Email, 5: Website")
    public ResponseEntity<BaseResponse<List<CustomerSupportResponse>>> getAllCustomerSupports(BaseFilterRequest req) {
        BaseFilterRequest filter = req != null ? req : new BaseFilterRequest();
        return successPage(customerSupportService.getAllCustomerSupports(filter), filter);
    }

    @GetMapping("/{id}")
    @RequiresPermission(module = "Customer Support", action = "View")
    @Operation(summary = "Get customer support by ID")
    public ResponseEntity<BaseResponse<CustomerSupportResponse>> getCustomerSupportById(@PathVariable Long id) {
        return success(customerSupportService.getCustomerSupportResponseById(id));
    }

    @PostMapping
    @RequiresPermission(module = "Customer Support", action = "Add")
    @Operation(summary = "Create new customer support")
    public ResponseEntity<BaseResponse<Void>> createCustomerSupport(@Valid @RequestBody CustomerSupportRequest request) {
        return successVoid(userId -> customerSupportService.createCustomerSupport(request, userId));
    }

    @PutMapping("/{id}")
    @RequiresPermission(module = "Customer Support", action = "Edit")
    @Operation(summary = "Update customer support")
    public ResponseEntity<BaseResponse<Void>> updateCustomerSupport(@PathVariable Long id, @Valid @RequestBody CustomerSupportRequest request) {
        return successVoid(userId -> customerSupportService.updateCustomerSupport(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission(module = "Customer Support", action = "Delete")
    @Operation(summary = "Delete customer support")
    public ResponseEntity<BaseResponse<Void>> deleteCustomerSupport(@PathVariable Long id) {
        return successVoid(() -> customerSupportService.deleteCustomerSupport(id));
    }
}
