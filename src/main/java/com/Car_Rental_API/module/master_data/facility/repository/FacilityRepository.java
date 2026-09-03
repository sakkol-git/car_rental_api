package com.Car_Rental_API.module.master_data.facility.repository;

import com.Car_Rental_API.module.master_data.facility.repository.*;
import com.Car_Rental_API.module.master_data.facility.mapper.*;
import com.Car_Rental_API.module.master_data.facility.service.*;
import com.Car_Rental_API.module.master_data.facility.model.*;
import com.Car_Rental_API.module.master_data.facility.dto.*;


import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.db_access.jooq.tables.records.FacilitiesRecord;
import com.Car_Rental_API.module.master_data.facility.model.Facility;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SelectFieldOrAsterisk;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.Car_Rental_API.common.util.QueryUtil;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import static com.Car_Rental_API.common.util.QueryUtil.addAuditJoins;
import static com.Car_Rental_API.common.util.QueryUtil.auditFields;
import static com.Car_Rental_API.common.util.QueryUtil.touchModified;
import static com.db_access.jooq.tables.Facilities.FACILITIES;

@Repository
@RequiredArgsConstructor
public class FacilityRepository {

    private final DSLContext dsl;

    // * Query Fields
    private List<SelectFieldOrAsterisk> facilityFields() {
        var fields = new ArrayList<SelectFieldOrAsterisk>(List.of(FACILITIES.asterisk()));
        fields.addAll(auditFields());
        return fields;
    }

    // * Keep soft-delete and keyword filters reusable for list/count queries.
    private Condition buildCondition(BaseFilterRequest req) {
        Condition condition = FACILITIES.IS_ACTIVE.eq((byte) 1);
        String keyword = req == null ? null : req.getKeyword();

        if (!StringUtils.hasText(keyword)) {
            return condition;
        }

        String likePattern = "%" + keyword.trim() + "%";
        return condition.and(
            FACILITIES.NAME_KH.likeIgnoreCase(likePattern)
                .or(FACILITIES.NAME_EN.likeIgnoreCase(likePattern))
                .or(FACILITIES.NAME_ZH.likeIgnoreCase(likePattern))
        );
    }

    public List<Facility> findAll(BaseFilterRequest req) {
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        return addAuditJoins(dsl.select(facilityFields()).from(FACILITIES), FACILITIES.getName())
                .where(buildCondition(req))
                .orderBy(FACILITIES.SORT_ORDER.asc(), FACILITIES.ID.desc())
                .limit(limit)
                .offset(offset)
                .fetchInto(Facility.class);
    }

    public long countAll(BaseFilterRequest req) {
        return QueryUtil.countAll(dsl, FACILITIES, buildCondition(req));
    }

    public Optional<Facility> findById(Long id) {
        return addAuditJoins(dsl.select(facilityFields()).from(FACILITIES), FACILITIES.getName())
                .where(FACILITIES.ID.eq(id))
                .and(FACILITIES.IS_ACTIVE.eq((byte) 1))
                .fetchOptionalInto(Facility.class);
    }

    public Facility save(Facility facility) {
        FacilitiesRecord record = dsl.newRecord(FACILITIES);
        record.from(facility);
        record.setId(null);
        record.setCreated(LocalDateTime.now());
        dsl.insertInto(FACILITIES).set(record).execute();

        facility.setId(dsl.lastID().longValue());
        return facility;
    }

    public void update(Facility facility) {
        FacilitiesRecord record = dsl.newRecord(FACILITIES);
        record.from(facility);
        touchModified(record, FACILITIES.MODIFIED, FACILITIES.MODIFIED_BY, facility.getModifiedBy());
        record.setId(facility.getId());
        record.changed(FACILITIES.ID, false);

        dsl.update(FACILITIES)
                .set(record)
                .where(FACILITIES.ID.eq(facility.getId()))
                .execute();
    }

    public int deleteById(Long id) {
        return dsl.update(FACILITIES)
                .set(FACILITIES.IS_ACTIVE, (byte) 0)
                .set(FACILITIES.MODIFIED, LocalDateTime.now())
                .where(FACILITIES.ID.eq(id))
                .execute();
    }


    // * Dropdown Operations
    public PageResult<DropdownResponse> findDropdown(BaseFilterRequest req) {
        Condition cond = buildCondition(req);
        long total = QueryUtil.countAll(dsl, FACILITIES, cond);
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        List<DropdownResponse> data = dsl.select(FACILITIES.ID, FACILITIES.NAME_EN.as("name"))
                .from(FACILITIES)
                .where(cond)
                .orderBy(FACILITIES.SORT_ORDER.asc(), FACILITIES.NAME_EN.asc())
                .limit(limit)
                .offset(offset)
                .fetchInto(DropdownResponse.class);

        return new PageResult<>(data, total);
    }

    public List<DropdownResponse> findDropdown() {
        return findDropdown(null).data();
    }
}
