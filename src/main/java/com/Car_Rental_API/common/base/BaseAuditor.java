package com.Car_Rental_API.common.base;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;


@Data
@SuperBuilder
@NoArgsConstructor
@Accessors(chain = true)
public abstract class BaseAuditor {

    // * Audit Fields
    private LocalDateTime created;
    private Long createdBy;
    private String createdByFullName;
    private LocalDateTime modified;
    private Long modifiedBy;
    private String modifiedByFullName;

    // * Status
    private Integer isActive;
}
