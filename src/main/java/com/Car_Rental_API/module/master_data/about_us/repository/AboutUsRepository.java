package com.Car_Rental_API.module.master_data.about_us.repository;

import com.Car_Rental_API.module.master_data.about_us.repository.*;
import com.Car_Rental_API.module.master_data.about_us.mapper.*;
import com.Car_Rental_API.module.master_data.about_us.service.*;
import com.Car_Rental_API.module.master_data.about_us.model.*;
import com.Car_Rental_API.module.master_data.about_us.dto.*;


import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.util.QueryUtil;
import com.db_access.jooq.tables.records.AboutUsRecord;
import com.Car_Rental_API.module.master_data.about_us.model.AboutUs;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SelectFieldOrAsterisk;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.Car_Rental_API.common.util.QueryUtil.addAuditJoins;
import static com.Car_Rental_API.common.util.QueryUtil.auditFields;
import static com.Car_Rental_API.common.util.QueryUtil.touchModified;
import static com.db_access.jooq.tables.AboutUs.ABOUT_US;

@Repository
@RequiredArgsConstructor
public class AboutUsRepository {

    private final DSLContext dsl;

    // * Select response fields with audit display names.
    private List<SelectFieldOrAsterisk> aboutUsFields() {
        var fields = new ArrayList<SelectFieldOrAsterisk>(List.of(ABOUT_US.asterisk()));
        fields.addAll(auditFields());
        return fields;
    }

    public Optional<AboutUs> findById(Long id) {
        return addAuditJoins(dsl.select(aboutUsFields()).from(ABOUT_US), ABOUT_US.getName())
                .where(ABOUT_US.ID.eq(id))
                .and(ABOUT_US.IS_ACTIVE.eq((byte) 1))
                .fetchOptionalInto(AboutUs.class);
    }

    public void save(AboutUs aboutUs) {
        AboutUsRecord record = dsl.newRecord(ABOUT_US);
        record.from(aboutUs);
        if (aboutUs.getId() != null) {
            record.setId(aboutUs.getId());
        }
        record.setCreated(aboutUs.getCreated() != null ? aboutUs.getCreated() : LocalDateTime.now());
        record.setCreatedBy(aboutUs.getCreatedBy());
        record.setModified(aboutUs.getModified() != null ? aboutUs.getModified() : LocalDateTime.now());
        record.setModifiedBy(aboutUs.getModifiedBy());
        record.setIsActive((byte) 1);

        dsl.insertInto(ABOUT_US).set(record).execute();
    }

    public void update(AboutUs aboutUs) {
        AboutUsRecord record = dsl.newRecord(ABOUT_US);
        record.from(aboutUs);
        touchModified(record, ABOUT_US.MODIFIED, ABOUT_US.MODIFIED_BY, aboutUs.getModifiedBy());
        record.setId(aboutUs.getId());
        record.changed(ABOUT_US.ID, false);

        dsl.update(ABOUT_US)
                .set(record)
                .where(ABOUT_US.ID.eq(aboutUs.getId()))
                .execute();
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> findDropdown(BaseFilterRequest req) {
        Condition cond = ABOUT_US.IS_ACTIVE.eq((byte) 1);
        if (req != null && req.getKeyword() != null && !req.getKeyword().trim().isEmpty()) {
            String like = "%" + req.getKeyword().trim() + "%";
            cond = cond.and(ABOUT_US.DESCRIPTION_EN.likeIgnoreCase(like)
                    .or(ABOUT_US.DESCRIPTION_KH.likeIgnoreCase(like))
                    .or(ABOUT_US.DESCRIPTION_ZH.likeIgnoreCase(like)));
        }
        long total = QueryUtil.countAll(dsl, ABOUT_US, cond);
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        List<DropdownResponse> data = dsl.select(ABOUT_US.ID, ABOUT_US.DESCRIPTION_EN.as("name"))
                .from(ABOUT_US)
                .where(cond)
                .limit(limit)
                .offset(offset)
                .fetchInto(DropdownResponse.class);

        return new PageResult<>(data, total);
    }

    public List<DropdownResponse> findDropdown() {
        return findDropdown(null).data();
    }
}
