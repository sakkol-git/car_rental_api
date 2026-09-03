package com.Car_Rental_API.security.authentication.user.repository;

import static com.db_access.jooq.Tables.USERS_API;
import static com.db_access.jooq.Tables.USER_GROUPS_API;
import static org.jooq.impl.DSL.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.util.QueryUtil;
import com.Car_Rental_API.security.authentication.user.dto.FilterUserRequest;
import com.Car_Rental_API.security.authentication.user.model.User;
import com.db_access.jooq.tables.records.UsersApiRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SelectFieldOrAsterisk;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final DSLContext dsl;

    private List<SelectFieldOrAsterisk> userFields() {
        List<SelectFieldOrAsterisk> fields = new ArrayList<>(List.of(
                USERS_API.ID,
                USERS_API.USERNAME,
                USERS_API.PASSWORD,
                USERS_API.FIRST_NAME.as("firstName"),
                USERS_API.LAST_NAME.as("lastName"),
                USERS_API.FULL_NAME.as("fullName"),
                USERS_API.PHOTO,
                USERS_API.SIGNATURE,
                USERS_API.IS_ACTIVE.as("isActive"),
                USERS_API.CREATED,
                USERS_API.CREATED_BY.as("createdBy"),
                USERS_API.MODIFIED,
                USERS_API.MODIFIED_BY.as("modifiedBy"),
                USERS_API.REFRESH_TOKEN.as("refreshToken"),
                USERS_API.REFRESH_TOKEN_EXPIRATION.as("refreshTokenExpiration"),
                USERS_API.PUSH_TOKEN.as("pushToken"),
                USERS_API.DEVICE_NAME.as("deviceName")));
        fields.addAll(QueryUtil.auditFields());
        return fields;
    }

    private Condition buildCondition(FilterUserRequest req) {
        Condition cond = USERS_API.IS_ACTIVE.notEqual((byte) 2);
        if (req != null) {
            if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
                var cr = USERS_API.as("cr");
                String kw = "%" + req.getKeyword().trim() + "%";
                cond = cond.and(USERS_API.USERNAME.likeIgnoreCase(kw)
                        .or(USERS_API.FULL_NAME.likeIgnoreCase(kw))
                        .or(cr.FULL_NAME.likeIgnoreCase(kw)));
            }
            if (req.getGroupId() != null) {
                cond = cond.and(exists(dsl.selectOne()
                        .from(USER_GROUPS_API)
                        .where(USER_GROUPS_API.USER_ID.eq(USERS_API.ID))
                        .and(USER_GROUPS_API.GROUP_ID.eq(req.getGroupId()))));
            }
        }
        return cond;
    }

    public List<User> findAll(FilterUserRequest req) {
        int size = (req != null) ? req.getSize() : 10;
        int offset = (req != null) ? (req.getPage() - 1) * size : 0;
        return QueryUtil.addAuditJoins(dsl.select(userFields()).from(USERS_API), USERS_API.getName())
                .where(buildCondition(req)).orderBy(USERS_API.ID.desc()).limit(size).offset(offset)
                .fetchInto(User.class);
    }

    public long countAll(FilterUserRequest req) {
        return QueryUtil.addAuditJoins(dsl.selectCount().from(USERS_API), USERS_API.getName())
                .where(buildCondition(req)).fetchOne(0, long.class);
    }

    public Optional<User> findById(Long id) {
        return QueryUtil.addAuditJoins(dsl.select(userFields()).from(USERS_API), USERS_API.getName())
                .where(USERS_API.ID.eq(id)).fetchOptionalInto(User.class);
    }

    public Optional<User> findByUsername(String username) {
        return QueryUtil.addAuditJoins(dsl.select(userFields()).from(USERS_API), USERS_API.getName())
                .where(USERS_API.USERNAME.eq(username)).and(USERS_API.IS_ACTIVE.ne((byte) 2))
                .fetchOptionalInto(User.class);
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> findDropdown(BaseFilterRequest req) {
        Condition cond = USERS_API.IS_ACTIVE.ne((byte) 2);
        if (req != null && req.getKeyword() != null && !req.getKeyword().trim().isEmpty()) {
            String like = "%" + req.getKeyword().trim() + "%";
            cond = cond.and(USERS_API.FULL_NAME.likeIgnoreCase(like).or(USERS_API.USERNAME.likeIgnoreCase(like)));
        }
        long total = QueryUtil.countAll(dsl, USERS_API, cond);
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        List<DropdownResponse> data = dsl.select(USERS_API.ID, USERS_API.FULL_NAME.as("name"))
                .from(USERS_API)
                .where(cond)
                .orderBy(USERS_API.FULL_NAME.asc(), USERS_API.USERNAME.asc())
                .limit(limit)
                .offset(offset)
                .fetchInto(DropdownResponse.class);

        return new PageResult<>(data, total);
    }

    public List<DropdownResponse> findDropdown() {
        return findDropdown(null).data();
    }

    // * Unified Save (Insert/Update)
    public User save(User user, boolean updatePassword) {
        UsersApiRecord record = dsl.newRecord(USERS_API);
        record.from(user);
        if (user.getId() == null) {
            record.setId(null);
            record.setCreated(LocalDateTime.now());
            dsl.insertInto(USERS_API).set(record).execute();
            user.setId(dsl.lastID().longValue());
        } else {
            record.changed(USERS_API.ID, false);
            if (!updatePassword) {
                record.changed(USERS_API.PASSWORD, false);
            }
            QueryUtil.touchModified(record, USERS_API.MODIFIED, USERS_API.MODIFIED_BY, user.getModifiedBy());
            dsl.update(USERS_API).set(record).where(USERS_API.ID.eq(user.getId())).execute();
        }
        return user;
    }

    public void updateSessionInfo(Long userId, String sid, LocalDateTime start) {
        dsl.update(USERS_API)
                .set(USERS_API.SESSION_ID, sid)
                .set(USERS_API.SESSION_START, start)
                .set(USERS_API.SESSION_ACTIVE, start)
                .where(USERS_API.ID.eq(userId))
                .execute();
    }

    public void updatePassword(Long userId, String password) {
        dsl.update(USERS_API).set(USERS_API.PASSWORD, password).where(USERS_API.ID.eq(userId)).execute();
    }

    public void updateRefreshToken(Long userId, String refreshToken, LocalDateTime expiration, String pushToken,
            String deviceName) {
        dsl.update(USERS_API)
                .set(USERS_API.REFRESH_TOKEN, refreshToken)
                .set(USERS_API.REFRESH_TOKEN_EXPIRATION, expiration)
                .set(USERS_API.PUSH_TOKEN, pushToken)
                .set(USERS_API.DEVICE_NAME, deviceName)
                .where(USERS_API.ID.eq(userId))
                .execute();
    }

    public Optional<User> findByRefreshToken(String refreshToken) {
        return QueryUtil.addAuditJoins(dsl.select(userFields()).from(USERS_API), USERS_API.getName())
                .where(USERS_API.REFRESH_TOKEN.eq(refreshToken))
                .and(USERS_API.IS_ACTIVE.ne((byte) 2))
                .fetchOptionalInto(User.class);
    }

}
