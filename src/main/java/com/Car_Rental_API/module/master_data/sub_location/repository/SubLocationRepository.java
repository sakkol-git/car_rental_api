package com.Car_Rental_API.module.master_data.sub_location.repository;

import com.Car_Rental_API.module.master_data.sub_location.repository.*;
import com.Car_Rental_API.module.master_data.sub_location.mapper.*;
import com.Car_Rental_API.module.master_data.sub_location.service.*;
import com.Car_Rental_API.module.master_data.sub_location.model.*;
import com.Car_Rental_API.module.master_data.sub_location.dto.*;


import com.Car_Rental_API.common.base_dto.response.DropdownResponse;

import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.util.QueryUtil;
import com.db_access.jooq.tables.records.SubLocationsRecord;
import com.Car_Rental_API.module.master_data.sub_location.model.SubLocation;
import com.Car_Rental_API.module.master_data.sub_location.dto.SubLocationFilterRequest;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SelectFieldOrAsterisk;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.Car_Rental_API.common.util.QueryUtil.addAuditJoins;
import static com.Car_Rental_API.common.util.QueryUtil.auditFields;
import static com.Car_Rental_API.common.util.QueryUtil.touchModified;
import static com.db_access.jooq.tables.Provinces.PROVINCES;
import static com.db_access.jooq.tables.SubLocations.SUB_LOCATIONS;

@Repository
@RequiredArgsConstructor
public class SubLocationRepository {

    private final DSLContext dsl;

    // * Query Fields
    private List<SelectFieldOrAsterisk> subLocationFields() {
        var fields = new ArrayList<SelectFieldOrAsterisk>(Arrays.asList(SUB_LOCATIONS.fields()));
        fields.add(PROVINCES.NAME.as("provinceName"));
        fields.addAll(auditFields());
        return fields;
    }

    private Condition buildCondition(SubLocationFilterRequest req) {
        Condition cond = SUB_LOCATIONS.IS_ACTIVE.eq((byte) 1);
        if (req != null) {
            if (req.getKeyword() != null && !req.getKeyword().trim().isEmpty()) {
                String likePattern = "%" + req.getKeyword().trim() + "%";
                cond = cond.and(
                    SUB_LOCATIONS.NAME.likeIgnoreCase(likePattern)
                        .or(PROVINCES.NAME.likeIgnoreCase(likePattern))
                );
            }
            if (req.getProvinceId() != null) {
                cond = cond.and(SUB_LOCATIONS.PROVINCE_ID.eq(req.getProvinceId()));
            }
            if (req.getIsPublic() != null) {
                cond = cond.and(SUB_LOCATIONS.IS_PUBLIC.eq(req.getIsPublic()));
            }
        }
        return cond;
    }

    public List<SubLocation> findAll(SubLocationFilterRequest req) {
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        var query = addAuditJoins(dsl.select(subLocationFields()).from(SUB_LOCATIONS), SUB_LOCATIONS.getName())
                .leftJoin(PROVINCES).on(PROVINCES.ID.eq(SUB_LOCATIONS.PROVINCE_ID));

        return query.where(buildCondition(req))
                .orderBy(SUB_LOCATIONS.ID.desc())
                .limit(limit)
                .offset(offset)
                .fetchInto(SubLocation.class);
    }

    public long countAll(SubLocationFilterRequest req) {
        return dsl.selectCount()
                .from(SUB_LOCATIONS)
                .leftJoin(PROVINCES).on(PROVINCES.ID.eq(SUB_LOCATIONS.PROVINCE_ID))
                .where(buildCondition(req))
                .fetchOne(0, long.class);
    }

    public Optional<SubLocation> findById(Long id) {
        var query = addAuditJoins(dsl.select(subLocationFields()).from(SUB_LOCATIONS), SUB_LOCATIONS.getName())
                .leftJoin(PROVINCES).on(PROVINCES.ID.eq(SUB_LOCATIONS.PROVINCE_ID));

        return query.where(SUB_LOCATIONS.ID.eq(id))
                .and(SUB_LOCATIONS.IS_ACTIVE.eq((byte) 1))
                .fetchOptionalInto(SubLocation.class);
    }

    public SubLocation save(SubLocation subLocation) {
        SubLocationsRecord record = dsl.newRecord(SUB_LOCATIONS);
        record.from(subLocation);
        record.setId(null);
        record.setCreated(LocalDateTime.now());
        dsl.insertInto(SUB_LOCATIONS).set(record).execute();

        subLocation.setId(dsl.lastID().longValue());
        return subLocation;
    }

    public void update(SubLocation subLocation) {
        SubLocationsRecord record = dsl.newRecord(SUB_LOCATIONS);
        record.from(subLocation);
        touchModified(record, SUB_LOCATIONS.MODIFIED, SUB_LOCATIONS.MODIFIED_BY, subLocation.getModifiedBy());
        record.setId(subLocation.getId());
        record.changed(SUB_LOCATIONS.ID, false);

        dsl.update(SUB_LOCATIONS)
                .set(record)
                .where(SUB_LOCATIONS.ID.eq(subLocation.getId()))
                .execute();
    }

    public void updateStatus(Long id, Byte isPublic, Long modifiedBy) {
        dsl.update(SUB_LOCATIONS)
                .set(SUB_LOCATIONS.IS_PUBLIC, isPublic)
                .set(SUB_LOCATIONS.MODIFIED, LocalDateTime.now())
                .set(SUB_LOCATIONS.MODIFIED_BY, modifiedBy)
                .where(SUB_LOCATIONS.ID.eq(id))
                .and(SUB_LOCATIONS.IS_ACTIVE.eq((byte) 1))
                .execute();
    }

    public void deleteById(Long id) {
        dsl.update(SUB_LOCATIONS)
                .set(SUB_LOCATIONS.IS_ACTIVE, (byte) 0)
                .set(SUB_LOCATIONS.MODIFIED, LocalDateTime.now())
                .where(SUB_LOCATIONS.ID.eq(id))
                .execute();
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> findDropdown(BaseFilterRequest req) {
        Condition cond = SUB_LOCATIONS.IS_ACTIVE.eq((byte) 1);
        if (req != null && req.getKeyword() != null && !req.getKeyword().trim().isEmpty()) {
            String like = "%" + req.getKeyword().trim() + "%";
            cond = cond.and(SUB_LOCATIONS.NAME.likeIgnoreCase(like));
        }
        if (req instanceof SubLocationFilterRequest subReq) {
            if (subReq.getProvinceId() != null) {
                cond = cond.and(SUB_LOCATIONS.PROVINCE_ID.eq(subReq.getProvinceId()));
            }
            if (subReq.getIsPublic() != null) {
                cond = cond.and(SUB_LOCATIONS.IS_PUBLIC.eq(subReq.getIsPublic()));
            }
        }
        long total = QueryUtil.countAll(dsl, SUB_LOCATIONS, cond);
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        List<DropdownResponse> data = dsl.select(SUB_LOCATIONS.ID, SUB_LOCATIONS.NAME.as("name"))
                .from(SUB_LOCATIONS)
                .where(cond)
                .orderBy(SUB_LOCATIONS.NAME.asc())
                .limit(limit)
                .offset(offset)
                .fetchInto(DropdownResponse.class);

        return new PageResult<>(data, total);
    }

    public List<DropdownResponse> findDropdown() {
        return findDropdown(null).data();
    }
}
