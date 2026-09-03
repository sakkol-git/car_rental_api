package com.Car_Rental_API.module.master_data.vehicle_brand.repository;

import com.Car_Rental_API.module.master_data.vehicle_brand.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_brand.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_brand.service.*;
import com.Car_Rental_API.module.master_data.vehicle_brand.model.*;
import com.Car_Rental_API.module.master_data.vehicle_brand.dto.*;


import com.Car_Rental_API.common.base_dto.response.DropdownResponse;

import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.util.QueryUtil;
import com.db_access.jooq.tables.records.VehicleBrandsRecord;
import com.Car_Rental_API.module.master_data.vehicle_brand.model.VehicleBrand;
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
import static com.db_access.jooq.tables.VehicleBrands.VEHICLE_BRANDS;

@Repository
@RequiredArgsConstructor
public class VehicleBrandRepository {

    private final DSLContext dsl;

    // * Query Fields
    private List<SelectFieldOrAsterisk> brandFields() {
        var fields = new ArrayList<SelectFieldOrAsterisk>(List.of(VEHICLE_BRANDS.asterisk()));
        fields.addAll(auditFields());
        return fields;
    }

    private Condition buildCondition(BaseFilterRequest req) {
        Condition cond = VEHICLE_BRANDS.IS_ACTIVE.eq((byte) 1);
        if (req != null && req.getKeyword() != null && !req.getKeyword().trim().isEmpty()) {
            String likePattern = "%" + req.getKeyword().trim() + "%";
            cond = cond.and(VEHICLE_BRANDS.NAME.likeIgnoreCase(likePattern));
        }
        return cond;
    }

    public List<VehicleBrand> findAll(BaseFilterRequest req) {
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        return addAuditJoins(dsl.select(brandFields()).from(VEHICLE_BRANDS), VEHICLE_BRANDS.getName())
                .where(buildCondition(req))
                .orderBy(VEHICLE_BRANDS.SORT_ORDER.asc(), VEHICLE_BRANDS.ID.desc())
                .limit(limit)
                .offset(offset)
                .fetchInto(VehicleBrand.class);
    }

    public long countAll(BaseFilterRequest req) {
        return QueryUtil.countAll(dsl, VEHICLE_BRANDS, buildCondition(req));
    }

    public Optional<VehicleBrand> findById(Long id) {
        return addAuditJoins(dsl.select(brandFields()).from(VEHICLE_BRANDS), VEHICLE_BRANDS.getName())
                .where(VEHICLE_BRANDS.ID.eq(id))
                .and(VEHICLE_BRANDS.IS_ACTIVE.eq((byte) 1))
                .fetchOptionalInto(VehicleBrand.class);
    }

    public VehicleBrand save(VehicleBrand brand) {
        VehicleBrandsRecord record = dsl.newRecord(VEHICLE_BRANDS);
        record.from(brand);
        record.setId(null);
        record.setCreated(LocalDateTime.now());
        dsl.insertInto(VEHICLE_BRANDS).set(record).execute();

        brand.setId(dsl.lastID().longValue());
        return brand;
    }

    public void update(VehicleBrand brand) {
        VehicleBrandsRecord record = dsl.newRecord(VEHICLE_BRANDS);
        record.from(brand);
        touchModified(record, VEHICLE_BRANDS.MODIFIED, VEHICLE_BRANDS.MODIFIED_BY, brand.getModifiedBy());
        record.setId(brand.getId());
        record.changed(VEHICLE_BRANDS.ID, false);

        dsl.update(VEHICLE_BRANDS).set(record).where(VEHICLE_BRANDS.ID.eq(brand.getId())).execute();
    }

    public void deleteById(Long id) {
        dsl.update(VEHICLE_BRANDS)
                .set(VEHICLE_BRANDS.IS_ACTIVE, (byte) 0)
                .set(VEHICLE_BRANDS.MODIFIED, LocalDateTime.now())
                .where(VEHICLE_BRANDS.ID.eq(id))
                .execute();
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> findDropdown(BaseFilterRequest req) {
        Condition cond = buildCondition(req);
        long total = QueryUtil.countAll(dsl, VEHICLE_BRANDS, cond);
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        List<DropdownResponse> data = dsl.select(VEHICLE_BRANDS.ID, VEHICLE_BRANDS.NAME.as("name"))
                .from(VEHICLE_BRANDS)
                .where(cond)
                .orderBy(VEHICLE_BRANDS.SORT_ORDER.asc(), VEHICLE_BRANDS.NAME.asc())
                .limit(limit)
                .offset(offset)
                .fetchInto(DropdownResponse.class);

        return new PageResult<>(data, total);
    }

    public List<DropdownResponse> findDropdown() {
        return findDropdown(null).data();
    }
}
