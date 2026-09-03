package com.Car_Rental_API.module.master_data.vehicle_model.repository;

import com.Car_Rental_API.module.master_data.vehicle_model.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_model.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_model.service.*;
import com.Car_Rental_API.module.master_data.vehicle_model.model.*;
import com.Car_Rental_API.module.master_data.vehicle_model.dto.*;


import com.Car_Rental_API.common.base_dto.response.DropdownResponse;

import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.util.QueryUtil;
import com.db_access.jooq.tables.records.VehicleModelsRecord;
import com.Car_Rental_API.module.master_data.vehicle_model.model.VehicleModel;
import com.Car_Rental_API.module.master_data.vehicle_model.dto.VehicleModelFilterRequest;
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
import static com.db_access.jooq.tables.VehicleModels.VEHICLE_MODELS;

@Repository
@RequiredArgsConstructor
public class VehicleModelRepository {

    private final DSLContext dsl;

    // * Query Fields
    private List<SelectFieldOrAsterisk> modelFields() {
        var fields = new ArrayList<SelectFieldOrAsterisk>(List.of(
            VEHICLE_MODELS.asterisk(),
            VEHICLE_BRANDS.NAME.as("brandName")
        ));
        fields.addAll(auditFields());
        return fields;
    }

    private Condition buildCondition(VehicleModelFilterRequest req) {
        Condition cond = VEHICLE_MODELS.IS_ACTIVE.eq((byte) 1);
        if (req != null) {
            if (req.getKeyword() != null && !req.getKeyword().trim().isEmpty()) {
                String likePattern = "%" + req.getKeyword().trim() + "%";
                cond = cond.and(
                    VEHICLE_MODELS.NAME.likeIgnoreCase(likePattern)
                        .or(VEHICLE_BRANDS.NAME.likeIgnoreCase(likePattern))
                );
            }
            if (req.getBrandId() != null) {
                cond = cond.and(VEHICLE_MODELS.BRAND_ID.eq(req.getBrandId()));
            }
        }
        return cond;
    }

    public List<VehicleModel> findAll(VehicleModelFilterRequest req) {
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        var query = addAuditJoins(dsl.select(modelFields()).from(VEHICLE_MODELS), VEHICLE_MODELS.getName())
                .leftJoin(VEHICLE_BRANDS).on(VEHICLE_BRANDS.ID.eq(VEHICLE_MODELS.BRAND_ID));

        return query.where(buildCondition(req))
                .orderBy(VEHICLE_MODELS.ID.desc())
                .limit(limit)
                .offset(offset)
                .fetchInto(VehicleModel.class);
    }

    public long countAll(VehicleModelFilterRequest req) {
        return dsl.selectCount()
                .from(VEHICLE_MODELS)
                .leftJoin(VEHICLE_BRANDS).on(VEHICLE_BRANDS.ID.eq(VEHICLE_MODELS.BRAND_ID))
                .where(buildCondition(req))
                .fetchOne(0, long.class);
    }

    public Optional<VehicleModel> findById(Long id) {
        var query = addAuditJoins(dsl.select(modelFields()).from(VEHICLE_MODELS), VEHICLE_MODELS.getName())
                .leftJoin(VEHICLE_BRANDS).on(VEHICLE_BRANDS.ID.eq(VEHICLE_MODELS.BRAND_ID));

        return query.where(VEHICLE_MODELS.ID.eq(id))
                .and(VEHICLE_MODELS.IS_ACTIVE.eq((byte) 1))
                .fetchOptionalInto(VehicleModel.class);
    }

    public boolean existsActiveBrand(Long brandId) {
        return dsl.fetchExists(VEHICLE_BRANDS, VEHICLE_BRANDS.ID.eq(brandId).and(VEHICLE_BRANDS.IS_ACTIVE.eq((byte) 1)));
    }

    public VehicleModel save(VehicleModel model) {
        VehicleModelsRecord record = dsl.newRecord(VEHICLE_MODELS);
        record.from(model);
        record.setId(null);
        record.setCreated(LocalDateTime.now());
        dsl.insertInto(VEHICLE_MODELS).set(record).execute();

        model.setId(dsl.lastID().longValue());
        return model;
    }

    public void update(VehicleModel model) {
        VehicleModelsRecord record = dsl.newRecord(VEHICLE_MODELS);
        record.from(model);
        touchModified(record, VEHICLE_MODELS.MODIFIED, VEHICLE_MODELS.MODIFIED_BY, model.getModifiedBy());
        record.setId(model.getId());
        record.changed(VEHICLE_MODELS.ID, false);

        dsl.update(VEHICLE_MODELS).set(record).where(VEHICLE_MODELS.ID.eq(model.getId())).execute();
    }

    public void deleteById(Long id) {
        dsl.update(VEHICLE_MODELS)
                .set(VEHICLE_MODELS.IS_ACTIVE, (byte) 0)
                .set(VEHICLE_MODELS.MODIFIED, LocalDateTime.now())
                .where(VEHICLE_MODELS.ID.eq(id))
                .execute();
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> findDropdown(BaseFilterRequest req) {
        Condition cond = VEHICLE_MODELS.IS_ACTIVE.eq((byte) 1);
        if (req != null && req.getKeyword() != null && !req.getKeyword().trim().isEmpty()) {
            String like = "%" + req.getKeyword().trim() + "%";
            cond = cond.and(VEHICLE_MODELS.NAME.likeIgnoreCase(like));
        }
        if (req instanceof VehicleModelFilterRequest modelReq && modelReq.getBrandId() != null) {
            cond = cond.and(VEHICLE_MODELS.BRAND_ID.eq(modelReq.getBrandId()));
        }
        long total = QueryUtil.countAll(dsl, VEHICLE_MODELS, cond);
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        List<DropdownResponse> data = dsl.select(VEHICLE_MODELS.ID, VEHICLE_MODELS.NAME.as("name"))
                .from(VEHICLE_MODELS)
                .where(cond)
                .orderBy(VEHICLE_MODELS.NAME.asc())
                .limit(limit)
                .offset(offset)
                .fetchInto(DropdownResponse.class);

        return new PageResult<>(data, total);
    }

    public List<DropdownResponse> findDropdown() {
        return findDropdown(null).data();
    }
}
