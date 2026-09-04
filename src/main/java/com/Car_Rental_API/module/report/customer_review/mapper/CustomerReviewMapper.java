package com.Car_Rental_API.module.report.customer_review.mapper;


import com.Car_Rental_API.module.report.customer_review.dto.response.CustomerReviewResponse;
import com.Car_Rental_API.module.report.customer_review.model.CustomerReview;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerReviewMapper {

    CustomerReviewResponse toResponse(CustomerReview review);

    List<CustomerReviewResponse> toResponses(List<CustomerReview> reviews);
}
