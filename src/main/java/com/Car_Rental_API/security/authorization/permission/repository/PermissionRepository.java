package com.Car_Rental_API.security.authorization.permission.repository;


import java.util.List;

import com.Car_Rental_API.security.authorization.permission.model.Permission;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;


import lombok.RequiredArgsConstructor;

import static com.db_access.jooq.Tables.PERMISSIONS_API;
import static com.db_access.jooq.Tables.USER_GROUPS_API;

@Repository
@RequiredArgsConstructor
public class PermissionRepository {

    private final DSLContext dsl;

    // * Find All Permissions for a Group
    public List<Permission> findByGroupId(Long groupId) {
        return dsl.selectFrom(PERMISSIONS_API).where(PERMISSIONS_API.GROUP_ID.eq(groupId)).fetchInto(Permission.class);
    }

    // * Find All Groups that have access to a Module
    public List<Permission> findByModuleId(Long moduleId) {
        return dsl.selectFrom(PERMISSIONS_API).where(PERMISSIONS_API.MODULE_ID.eq(moduleId)).fetchInto(Permission.class);
    }

    // * Check if Group has Permission for Module
    public boolean hasPermission(Long groupId, Long moduleId) {
        return dsl.fetchExists(dsl.selectFrom(PERMISSIONS_API).where(PERMISSIONS_API.GROUP_ID.eq(groupId)).and(PERMISSIONS_API.MODULE_ID.eq(moduleId)));
    }

    // * Get Module IDs for User (via user's groups)
    public List<Long> findModuleIdsByUserId(Long userId) {
        return dsl.selectDistinct(PERMISSIONS_API.MODULE_ID)
                .from(PERMISSIONS_API)
                .where(PERMISSIONS_API.GROUP_ID.in(
                        dsl.select(USER_GROUPS_API.GROUP_ID)
                                .from(USER_GROUPS_API)
                                .where(USER_GROUPS_API.USER_ID.eq(userId))
                ))
                .fetch(PERMISSIONS_API.MODULE_ID, Long.class);
    }

    // * Check if User has access to a Module
    public boolean userHasModuleAccess(Long userId, Long moduleId) {
        return dsl.fetchExists(
                dsl.selectFrom(PERMISSIONS_API)
                        .where(PERMISSIONS_API.MODULE_ID.eq(moduleId))
                        .and(PERMISSIONS_API.GROUP_ID.in(
                                dsl.select(USER_GROUPS_API.GROUP_ID)
                                        .from(USER_GROUPS_API)
                                        .where(USER_GROUPS_API.USER_ID.eq(userId))
                        ))
        );
    }

    // * Assign Permission
    public void assignPermission(Long groupId, Long moduleId) {
        if (!hasPermission(groupId, moduleId)) {
            dsl.insertInto(PERMISSIONS_API).set(PERMISSIONS_API.GROUP_ID, groupId).set(PERMISSIONS_API.MODULE_ID, moduleId).execute();
        }
    }

    // * Revoke Permission
    public void revokePermission(Long groupId, Long moduleId) {
        dsl.deleteFrom(PERMISSIONS_API).where(PERMISSIONS_API.GROUP_ID.eq(groupId)).and(PERMISSIONS_API.MODULE_ID.eq(moduleId)).execute();
    }

    // * Revoke All Permissions from Group (for bulk updates)
    public void removeAllPermissionsFromGroup(Long groupId) {
        dsl.deleteFrom(PERMISSIONS_API).where(PERMISSIONS_API.GROUP_ID.eq(groupId)).execute();
    }

    // * Delete all permissions associated with a module
    public void deleteByModuleId(Long moduleId) {
        dsl.deleteFrom(PERMISSIONS_API).where(PERMISSIONS_API.MODULE_ID.eq(moduleId)).execute();
    }

    // * Delete all permissions
    public void deleteAll() {
        dsl.deleteFrom(PERMISSIONS_API).execute();
    }

    // * Truncate permissions table
    public void truncate() {
        dsl.truncate(PERMISSIONS_API).execute();
    }

    // * Truncate permissions, modules, and module_types tables safely
    public void truncateAllModuleData() {
        dsl.execute("SET FOREIGN_KEY_CHECKS = 0");
        dsl.execute("TRUNCATE TABLE permissions_api");
        dsl.execute("TRUNCATE TABLE modules_api");
        dsl.execute("TRUNCATE TABLE module_types_api");
        dsl.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    // * Assign User ID 1 to Group ID 1 and grant all system modules to Group ID 1
    public void assignAllModulesToSuperAdminGroup() {
        dsl.execute("INSERT IGNORE INTO user_groups_api (user_id, group_id) VALUES (1, 1)");
        dsl.deleteFrom(PERMISSIONS_API).where(PERMISSIONS_API.GROUP_ID.eq(1L)).execute();
        dsl.execute("INSERT INTO permissions_api (group_id, module_id) SELECT 1 AS group_id, id AS module_id FROM modules_api");
    }
}
