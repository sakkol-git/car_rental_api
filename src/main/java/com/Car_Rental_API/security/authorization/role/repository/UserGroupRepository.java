package com.Car_Rental_API.security.authorization.role.repository;


import java.util.List;

import com.Car_Rental_API.security.authorization.role.model.UserGroup;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;


import lombok.RequiredArgsConstructor;

import static com.db_access.jooq.Tables.USER_GROUPS_API;

@Repository
@RequiredArgsConstructor
public class UserGroupRepository {

    private final DSLContext dsl;

    // * Find All Groups for a User
    public List<Long> findGroupIdsByUserId(Long userId) {
        return dsl.select(USER_GROUPS_API.GROUP_ID).from(USER_GROUPS_API).where(USER_GROUPS_API.USER_ID.eq(userId)).fetch(USER_GROUPS_API.GROUP_ID, Long.class);
    }

    // * Find All Users in a Group
    public List<Long> findUserIdsByGroupId(Long groupId) {
        return dsl.select(USER_GROUPS_API.USER_ID).from(USER_GROUPS_API).where(USER_GROUPS_API.GROUP_ID.eq(groupId)).fetch(USER_GROUPS_API.USER_ID, Long.class);
    }

    // * Check if User is in Group
    public boolean isUserInGroup(Long userId, Long groupId) {
        return dsl.fetchExists(dsl.selectFrom(USER_GROUPS_API).where(USER_GROUPS_API.USER_ID.eq(userId)).and(USER_GROUPS_API.GROUP_ID.eq(groupId)));
    }

    // * Assign User to Group
    public void assignUserToGroup(Long userId, Long groupId) {
        if (!isUserInGroup(userId, groupId)) {
            dsl.insertInto(USER_GROUPS_API).set(USER_GROUPS_API.USER_ID, userId).set(USER_GROUPS_API.GROUP_ID, groupId).execute();
        }
    }

    // * Remove User from Group
    public void removeUserFromGroup(Long userId, Long groupId) {
        dsl.deleteFrom(USER_GROUPS_API).where(USER_GROUPS_API.USER_ID.eq(userId)).and(USER_GROUPS_API.GROUP_ID.eq(groupId)).execute();
    }

    // * Remove All Users from Group (for bulk updates)
    public void removeAllUsersFromGroup(Long groupId) {
        dsl.deleteFrom(USER_GROUPS_API).where(USER_GROUPS_API.GROUP_ID.eq(groupId)).execute();
    }
    // * Remove All Groups from a User
    public void removeAllGroupsFromUser(Long userId) {
        dsl.deleteFrom(USER_GROUPS_API).where(USER_GROUPS_API.USER_ID.eq(userId)).execute();
    }

    // * Get User-Group mappings
    public List<UserGroup> findByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();
        return dsl.selectFrom(USER_GROUPS_API).where(USER_GROUPS_API.USER_ID.in(userIds)).fetchInto(UserGroup.class);
    }

    public List<UserGroup> findByGroupId(Long groupId) {
        return dsl.selectFrom(USER_GROUPS_API).where(USER_GROUPS_API.GROUP_ID.eq(groupId)).fetchInto(UserGroup.class);
    }
}
