package com.Car_Rental_API.module.master_data.customer.controller;

import com.Car_Rental_API.module.master_data.customer.repository.*;
import com.Car_Rental_API.module.master_data.customer.mapper.*;
import com.Car_Rental_API.module.master_data.customer.service.*;
import com.Car_Rental_API.module.master_data.customer.model.*;
import com.Car_Rental_API.module.master_data.customer.dto.*;



import com.Car_Rental_API.common.base.BaseController;
import com.Car_Rental_API.common.base_dto.response.BaseResponse;
import com.Car_Rental_API.module.master_data.customer.dto.CustomerRequest;
import com.Car_Rental_API.module.master_data.customer.dto.CustomerResponse;
import com.Car_Rental_API.module.master_data.customer.dto.CustomerFilterRequest;
import com.Car_Rental_API.module.master_data.customer.service.CustomerService;
import com.Car_Rental_API.security.authorization.util.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Tag(name = "24. Customer", description = "Customer Management APIs")
public class CustomerController extends BaseController {

    private final CustomerService customerService;

    @GetMapping
    @RequiresPermission(module = "Customer", action = "View")
    @Operation(summary = "Get all customers")
    public ResponseEntity<BaseResponse<List<CustomerResponse>>> getAllCustomers(@Valid CustomerFilterRequest request) {
        CustomerFilterRequest req = request != null ? request : new CustomerFilterRequest();
        return successPage(customerService.getAllCustomers(req), req);
    }

    @GetMapping("/{id}")
    @RequiresPermission(module = "Customer", action = "View")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<BaseResponse<CustomerResponse>> getCustomerById(@PathVariable Long id) {
        return success(customerService.getCustomerResponseById(id));
    }

    @PostMapping
    @RequiresPermission(module = "Customer", action = "Add")
    @Operation(summary = "Create new customer")
    public ResponseEntity<BaseResponse<CustomerResponse>> createCustomer(@Valid @RequestBody CustomerRequest request) {
        return success(customerService.createCustomer(request));
    }

    @PutMapping("/{id}")
    @RequiresPermission(module = "Customer", action = "Edit")
    @Operation(summary = "Update customer")
    public ResponseEntity<BaseResponse<Void>> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return successVoid(() -> customerService.updateCustomer(id, request));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission(module = "Customer", action = "Delete")
    @Operation(summary = "Delete customer")
    public ResponseEntity<BaseResponse<Void>> deleteCustomer(@PathVariable Long id) {
        return successVoid(() -> customerService.deleteCustomer(id));
    }
}