package com.Car_Rental_API.module.master_data.privacy_term.repository;

import com.Car_Rental_API.module.master_data.privacy_term.repository.*;
import com.Car_Rental_API.module.master_data.privacy_term.mapper.*;
import com.Car_Rental_API.module.master_data.privacy_term.service.*;
import com.Car_Rental_API.module.master_data.privacy_term.model.*;
import com.Car_Rental_API.module.master_data.privacy_term.dto.*;


import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.util.QueryUtil;
import com.Car_Rental_API.module.master_data.privacy_term.model.PrivacyTerm;
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
import static com.db_access.jooq.tables.PrivacyTerms.PRIVACY_TERMS;

@Repository
@RequiredArgsConstructor
public class PrivacyTermRepository {

    private final DSLContext dsl;

    // * Select response fields with audit display names.
    private List<SelectFieldOrAsterisk> privacyTermFields() {
        var fields = new ArrayList<SelectFieldOrAsterisk>(List.of(PRIVACY_TERMS.asterisk()));
        fields.addAll(auditFields());
        return fields;
    }

    public Optional<PrivacyTerm> findByType(Long type) {
        return addAuditJoins(dsl.select(privacyTermFields()).from(PRIVACY_TERMS), PRIVACY_TERMS.getName())
                .where(PRIVACY_TERMS.TYPE.eq(toByte(type)))
                .and(PRIVACY_TERMS.IS_ACTIVE.eq((byte) 1))
                .fetchOptionalInto(PrivacyTerm.class);
    }

    public void save(PrivacyTerm privacyTerm) {
        var record = dsl.newRecord(PRIVACY_TERMS);
        record.setType(toByte(privacyTerm.getType()));
        record.setDescriptionKh(privacyTerm.getDescriptionKh());
        record.setDescriptionEn(privacyTerm.getDescriptionEn());
        record.setDescriptionZh(privacyTerm.getDescriptionZh());
        record.setCreated(privacyTerm.getCreated() != null ? privacyTerm.getCreated() : LocalDateTime.now());
        record.setCreatedBy(privacyTerm.getCreatedBy());
        record.setModified(privacyTerm.getModified() != null ? privacyTerm.getModified() : LocalDateTime.now());
        record.setModifiedBy(privacyTerm.getModifiedBy());
        record.setIsActive((byte) 1);

        dsl.insertInto(PRIVACY_TERMS).set(record).execute();
    }

    public void update(PrivacyTerm privacyTerm) {
        dsl.update(PRIVACY_TERMS)
                .set(PRIVACY_TERMS.TYPE, toByte(privacyTerm.getType()))
                .set(PRIVACY_TERMS.DESCRIPTION_KH, privacyTerm.getDescriptionKh())
                .set(PRIVACY_TERMS.DESCRIPTION_EN, privacyTerm.getDescriptionEn())
                .set(PRIVACY_TERMS.DESCRIPTION_ZH, privacyTerm.getDescriptionZh())
                .set(PRIVACY_TERMS.MODIFIED, privacyTerm.getModified())
                .set(PRIVACY_TERMS.MODIFIED_BY, privacyTerm.getModifiedBy())
                .where(PRIVACY_TERMS.ID.eq(privacyTerm.getId()))
                .execute();
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> findDropdown(BaseFilterRequest req) {
        Condition cond = PRIVACY_TERMS.IS_ACTIVE.eq((byte) 1);
        if (req != null && req.getKeyword() != null && !req.getKeyword().trim().isEmpty()) {
            String like = "%" + req.getKeyword().trim() + "%";
            cond = cond.and(PRIVACY_TERMS.TYPE.likeIgnoreCase(like));
        }
        long total = QueryUtil.countAll(dsl, PRIVACY_TERMS, cond);
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        List<DropdownResponse> data = dsl.select(PRIVACY_TERMS.ID, PRIVACY_TERMS.TYPE.as("name"))
                .from(PRIVACY_TERMS)
                .where(cond)
                .orderBy(PRIVACY_TERMS.TYPE.asc())
                .limit(limit)
                .offset(offset)
                .fetchInto(DropdownResponse.class);

        return new PageResult<>(data, total);
    }

    public List<DropdownResponse> findDropdown() {
        return findDropdown(null).data();
    }

    private Byte toByte(Long value) {
        return value == null ? null : value.byteValue();
    }
}
