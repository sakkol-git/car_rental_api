package com.Car_Rental_API.module.report.customer_review.service;




import com.Car_Rental_API.module.report.customer_review.mapper.CustomerReviewMapper;
import com.Car_Rental_API.module.report.customer_review.repository.CustomerReviewRepository;
import com.Car_Rental_API.module.report.customer_review.dto.response.CustomerReviewResponse;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.util.QueryUtil;
import com.Car_Rental_API.module.master_data.customer.service.CustomerService;
import com.Car_Rental_API.module.master_data.customer.model.Customer;
import com.Car_Rental_API.module.report.customer_review.model.CustomerReview;
import com.Car_Rental_API.module.report.customer_review.dto.request.CustomerReviewFilterRequest;
import com.Car_Rental_API.module.report.customer_review.dto.request.CustomerReviewRequest;

import com.Car_Rental_API.security.authentication.util.AuthSystemWebhookClient;
import com.Car_Rental_API.security.authentication.auth.dto.AuthUserResponse;
import com.Car_Rental_API.common.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerReviewService {

    private final CustomerReviewRepository reviewRepository;
    private final CustomerReviewMapper reviewMapper;
    private final CustomerService customerService;
    private final AuthSystemWebhookClient authWebhookClient;

    // * Admin: paged list with filters (dateFrom, dateTo, keyword, vehicle, customer)
    public PageResult<CustomerReviewResponse> getAll(CustomerReviewFilterRequest req) {
        CustomerReviewFilterRequest filter = req != null ? req : new CustomerReviewFilterRequest();
        long total = QueryUtil.shouldCount(filter.getPage(), () -> reviewRepository.countAll(filter));
        return new PageResult<>(reviewMapper.toResponses(reviewRepository.findAll(filter)), total);
    }

    public CustomerReviewResponse getById(Long id) {
        return reviewMapper.toResponse(reviewRepository.findById(id)
                .orElseThrow(() -> new GlobalException("Review not found", 404)));
    }

    // * Admin: toggle review visibility on app (0=enabled, 1=disabled)
    @Transactional
    public void setDisabled(Long id, Byte isDisabled) {
        reviewRepository.findById(id).orElseThrow(() -> new GlobalException("Review not found", 404));
        reviewRepository.updateDisabled(id, isDisabled);
    }

    @Transactional
    public void delete(Long id) {
        reviewRepository.findById(id).orElseThrow(() -> new GlobalException("Review not found", 404));
        reviewRepository.deleteById(id);
    }

    // * Mobile: submit review — customer is resolved from auth token, not sent by client
    @Transactional
    public CustomerReviewResponse submitFromApp(String rawToken, CustomerReviewRequest req) {
        AuthUserResponse authUser = authWebhookClient.verifyAndGetUserInfo(rawToken)
                .orElseThrow(() -> new GlobalException("Invalid or unverified user token", 401));

        Long userId = parseUserId(authUser.getUserId());
        String name = (authUser.getFullName() != null && !authUser.getFullName().isBlank()) ? authUser.getFullName().trim() : "App Customer";
        String phone = (authUser.getPhone() != null && !authUser.getPhone().isBlank()) ? authUser.getPhone().trim() : "";

        Customer customer = customerService.getOrCreateCustomer(userId, name, phone, authUser.getEmail(), authUser.getOsType());

        CustomerReview review = new CustomerReview();
        review.setCustomerId(customer.getId());
        review.setVehicleId(req.getVehicleId());
        review.setSalesOrderId(req.getSalesOrderId());
        review.setRatingStars(req.getRatingStars());
        review.setComment(req.getComment());

        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    private Long parseUserId(String val) {
        if (val == null || val.isBlank()) return null;
        try { return Long.parseLong(val.trim()); } catch (Exception e) { return null; }
    }
}
