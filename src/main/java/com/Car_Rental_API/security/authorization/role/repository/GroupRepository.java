package com.Car_Rental_API.security.authorization.role.repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.util.QueryUtil;
import com.db_access.jooq.tables.records.GroupsApiRecord;
import com.Car_Rental_API.security.authorization.role.model.Group;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SelectFieldOrAsterisk;
import org.springframework.stereotype.Repository;


import lombok.RequiredArgsConstructor;

import static com.db_access.jooq.Tables.*;

@Repository
@RequiredArgsConstructor
public class GroupRepository {

    private final DSLContext dsl;

    private List<SelectFieldOrAsterisk> groupFields() {
        var fields = new java.util.ArrayList<SelectFieldOrAsterisk>(List.of(
                GROUPS_API.ID, GROUPS_API.NAME, GROUPS_API.IS_ACTIVE.as("isActive"),
                GROUPS_API.CREATED, GROUPS_API.MODIFIED,
                GROUPS_API.CREATED_BY.as("createdBy"), GROUPS_API.MODIFIED_BY.as("modifiedBy")));
        fields.addAll(QueryUtil.auditFields());
        return fields;
    }

    private Condition buildCondition(BaseFilterRequest req) {
        Condition cond = GROUPS_API.IS_ACTIVE.eq((byte)1);
        if (req != null && req.getKeyword() != null && !req.getKeyword().isBlank())
            cond = cond.and(GROUPS_API.NAME.likeIgnoreCase("%" + req.getKeyword().trim() + "%"));
        return cond;
    }

    public List<Group> findAll(BaseFilterRequest req) {
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;
        return QueryUtil.addAuditJoins(dsl.select(groupFields()).from(GROUPS_API), GROUPS_API.getName())
                .where(buildCondition(req)).orderBy(GROUPS_API.ID.desc()).limit(limit).offset(offset).fetchInto(Group.class);
    }

    public List<Group> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return QueryUtil.addAuditJoins(dsl.select(groupFields()).from(GROUPS_API), GROUPS_API.getName())
                .where(GROUPS_API.ID.in(ids)).fetchInto(Group.class);
    }

    public long countAll(BaseFilterRequest req) {
        return QueryUtil.countAll(dsl, GROUPS_API, buildCondition(req));
    }

    public Optional<Group> findById(Long id) {
        return QueryUtil.addAuditJoins(dsl.select(groupFields()).from(GROUPS_API), GROUPS_API.getName())
                .where(GROUPS_API.ID.eq(id)).fetchOptionalInto(Group.class);
    }

    // * Bulk-fetch users grouped by group ID
    public Map<Long, List<DropdownResponse>> findUsersByGroupIds(List<Long> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) return Map.of();
        return dsl.select(USER_GROUPS_API.GROUP_ID, USERS_API.ID, USERS_API.FULL_NAME.as("name"))
                .from(USER_GROUPS_API).join(USERS_API).on(USER_GROUPS_API.USER_ID.eq(USERS_API.ID))
                .where(USER_GROUPS_API.GROUP_ID.in(groupIds)).and(USERS_API.IS_ACTIVE.ne((byte) 2))
                .fetchGroups(USER_GROUPS_API.GROUP_ID, r -> new DropdownResponse(r.get(USERS_API.ID), r.get("name", String.class)));
    }

    public List<DropdownResponse> findUsersByGroupId(Long id) {
        return dsl.select(USERS_API.ID, USERS_API.FULL_NAME.as("name"))
                .from(USER_GROUPS_API).join(USERS_API).on(USER_GROUPS_API.USER_ID.eq(USERS_API.ID))
                .where(USER_GROUPS_API.GROUP_ID.eq(id)).and(USERS_API.IS_ACTIVE.ne((byte)2))
                .fetch(r -> new DropdownResponse(r.get(USERS_API.ID), r.get("name", String.class)));
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> findDropdown(BaseFilterRequest req) {
        Condition cond = buildCondition(req);
        long total = QueryUtil.countAll(dsl, GROUPS_API, cond);
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        List<DropdownResponse> data = dsl.select(GROUPS_API.ID, GROUPS_API.NAME.as("name"))
                .from(GROUPS_API)
                .where(cond)
                .orderBy(GROUPS_API.NAME.asc())
                .limit(limit)
                .offset(offset)
                .fetchInto(DropdownResponse.class);

        return new PageResult<>(data, total);
    }

    public List<DropdownResponse> findDropdown() {
        return findDropdown(null).data();
    }

    public com.Car_Rental_API.security.authorization.role.model.Group save(com.Car_Rental_API.security.authorization.role.model.Group group) {
        GroupsApiRecord record = dsl.newRecord(GROUPS_API);
        record.from(group);
        if (group.getId() == null) {
            record.setId(null);
            record.setCreated(LocalDateTime.now());
            dsl.insertInto(GROUPS_API).set(record).execute();
            group.setId(dsl.lastID().longValue());
        } else {
            record.changed(GROUPS_API.ID, false);
            QueryUtil.touchModified(record, GROUPS_API.MODIFIED, GROUPS_API.MODIFIED_BY, group.getModifiedBy());
            dsl.update(GROUPS_API).set(record).where(GROUPS_API.ID.eq(group.getId())).execute();
        }
        return group;
    }
}
