package com.Car_Rental_API.module.master_data.customer_support.repository;

import com.Car_Rental_API.module.master_data.customer_support.repository.*;
import com.Car_Rental_API.module.master_data.customer_support.mapper.*;
import com.Car_Rental_API.module.master_data.customer_support.service.*;
import com.Car_Rental_API.module.master_data.customer_support.model.*;
import com.Car_Rental_API.module.master_data.customer_support.dto.*;


import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.db_access.jooq.tables.records.CustomerSupportRecord;
import com.Car_Rental_API.module.master_data.customer_support.model.CustomerSupport;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SelectFieldOrAsterisk;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.Car_Rental_API.common.util.QueryUtil;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import static com.Car_Rental_API.common.util.QueryUtil.addAuditJoins;
import static com.Car_Rental_API.common.util.QueryUtil.auditFields;
import static com.Car_Rental_API.common.util.QueryUtil.touchModified;
import static com.db_access.jooq.tables.CustomerSupport.CUSTOMER_SUPPORT;

@Repository
@RequiredArgsConstructor
public class CustomerSupportRepository {

    private final DSLContext dsl;

    // * Query Fields
    private List<SelectFieldOrAsterisk> customerSupportFields() {
        var fields = new ArrayList<SelectFieldOrAsterisk>(List.of(CUSTOMER_SUPPORT.asterisk()));
        fields.addAll(auditFields());
        return fields;
    }

    private Condition buildCondition(BaseFilterRequest req) {
        Condition cond = CUSTOMER_SUPPORT.IS_ACTIVE.eq((byte) 1);
        if (req != null && req.getKeyword() != null && !req.getKeyword().trim().isEmpty()) {
            String likePattern = "%" + req.getKeyword().trim() + "%";
            cond = cond.and(
                CUSTOMER_SUPPORT.NAME_KH.likeIgnoreCase(likePattern)
                    .or(CUSTOMER_SUPPORT.NAME_EN.likeIgnoreCase(likePattern))
                    .or(CUSTOMER_SUPPORT.NAME_ZH.likeIgnoreCase(likePattern))
                    .or(CUSTOMER_SUPPORT.PHONE_NUMBER.likeIgnoreCase(likePattern))
                    .or(CUSTOMER_SUPPORT.LINK.likeIgnoreCase(likePattern))
            );
        }
        return cond;
    }

    public List<CustomerSupport> findAll(BaseFilterRequest req) {
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        return addAuditJoins(dsl.select(customerSupportFields()).from(CUSTOMER_SUPPORT), CUSTOMER_SUPPORT.getName())
                .where(buildCondition(req))
                .orderBy(CUSTOMER_SUPPORT.SORT_ORDER.asc(), CUSTOMER_SUPPORT.ID.desc())
                .limit(limit)
                .offset(offset)
                .fetchInto(CustomerSupport.class);
    }

    public long countAll(BaseFilterRequest req) {
        return QueryUtil.countAll(dsl, CUSTOMER_SUPPORT, buildCondition(req));
    }

    public Optional<CustomerSupport> findById(Long id) {
        return addAuditJoins(dsl.select(customerSupportFields()).from(CUSTOMER_SUPPORT), CUSTOMER_SUPPORT.getName())
                .where(CUSTOMER_SUPPORT.ID.eq(id))
                .and(CUSTOMER_SUPPORT.IS_ACTIVE.eq((byte) 1))
                .fetchOptionalInto(CustomerSupport.class);
    }

    public CustomerSupport save(CustomerSupport customerSupport) {
        CustomerSupportRecord record = dsl.newRecord(CUSTOMER_SUPPORT);
        record.from(customerSupport);
        record.setId(null);
        record.setCreated(LocalDateTime.now());
        dsl.insertInto(CUSTOMER_SUPPORT).set(record).execute();

        customerSupport.setId(dsl.lastID().longValue());
        return customerSupport;
    }

    public void update(CustomerSupport customerSupport) {
        CustomerSupportRecord record = dsl.newRecord(CUSTOMER_SUPPORT);
        record.from(customerSupport);
        touchModified(record, CUSTOMER_SUPPORT.MODIFIED, CUSTOMER_SUPPORT.MODIFIED_BY, customerSupport.getModifiedBy());
        record.setId(customerSupport.getId());
        record.changed(CUSTOMER_SUPPORT.ID, false);

        dsl.update(CUSTOMER_SUPPORT)
                .set(record)
                .where(CUSTOMER_SUPPORT.ID.eq(customerSupport.getId()))
                .execute();
    }

    public void deleteById(Long id) {
        dsl.update(CUSTOMER_SUPPORT)
                .set(CUSTOMER_SUPPORT.IS_ACTIVE, (byte) 0)
                .set(CUSTOMER_SUPPORT.MODIFIED, LocalDateTime.now())
                .where(CUSTOMER_SUPPORT.ID.eq(id))
                .execute();
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> findDropdown(BaseFilterRequest req) {
        Condition cond = buildCondition(req);
        long total = QueryUtil.countAll(dsl, CUSTOMER_SUPPORT, cond);
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        List<DropdownResponse> data = dsl.select(CUSTOMER_SUPPORT.ID, CUSTOMER_SUPPORT.NAME_EN.as("name"))
                .from(CUSTOMER_SUPPORT)
                .where(cond)
                .orderBy(CUSTOMER_SUPPORT.SORT_ORDER.asc(), CUSTOMER_SUPPORT.NAME_EN.asc())
                .limit(limit)
                .offset(offset)
                .fetchInto(DropdownResponse.class);

        return new PageResult<>(data, total);
    }

    public List<DropdownResponse> findDropdown() {
        return findDropdown(null).data();
    }
}
