package com.Car_Rental_API.security.authorization.role.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.exception.GlobalException;
import com.Car_Rental_API.common.util.QueryUtil;
import com.Car_Rental_API.security.authorization.permission.repository.PermissionRepository;
import com.Car_Rental_API.security.authorization.permission.service.PermissionService;
import com.Car_Rental_API.security.authorization.role.dto.GroupRequest;
import com.Car_Rental_API.security.authorization.role.dto.GroupResponse;
import com.Car_Rental_API.security.authorization.role.mapper.GroupMapper;
import com.Car_Rental_API.security.authorization.role.model.Group;
import com.Car_Rental_API.security.authorization.role.model.UserGroup;
import com.Car_Rental_API.security.authorization.role.repository.GroupRepository;
import com.Car_Rental_API.security.authorization.role.repository.UserGroupRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;
    private final PermissionRepository permissionRepository;
    private final PermissionService permissionService;
    private final GroupMapper groupMapper;

    // * List with pagination and user-to-group hydration
    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "groups", key = "#req != null ? #req.toString() : 'default'")
    public PageResult<GroupResponse> getAllActiveGroups(BaseFilterRequest req) {
        BaseFilterRequest filter = req != null ? req : new BaseFilterRequest();
        List<Group> groups = groupRepository.findAll(filter);
        long total = QueryUtil.shouldCount(filter.getPage(), () -> groupRepository.countAll(filter));
        var groupUsers = groupRepository.findUsersByGroupIds(groups.stream().map(Group::getId).toList());
        List<GroupResponse> list = groups.stream().map(g -> {
            GroupResponse res = groupMapper.toResponse(g);
            res.setUsers(groupUsers.getOrDefault(g.getId(), List.of()));
            return res;
        }).toList();
        return new PageResult<>(list, total);
    }

    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "group", key = "#id")
    public Group getGroupById(Long id) {
        return groupRepository.findById(id).orElseThrow(() -> new GlobalException("Group not found", 404));
    }

    // * Enriched response: includes users and module tree
    public GroupResponse getGroupResponseById(Long id) {
        GroupResponse res = groupMapper.toResponse(getGroupById(id));
        res.setUsers(groupRepository.findUsersByGroupId(id));
        res.setModuleTypeList(permissionService.getAllModulesTree(null, id));
        return res;
    }

    @Cacheable(value = "userGroups", key = "#userId")
    public List<Group> getUserGroups(Long userId) {
        return userGroupRepository.findGroupIdsByUserId(userId).stream()
                .map(groupRepository::findById).filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toList());
    }

    public Map<Long, List<Group>> getUserGroupsBulk(List<Long> userIds) {
        var mappings = userGroupRepository.findByUserIds(userIds);
        var groupsMap = groupRepository.findAllByIds(mappings.stream().map(UserGroup::getGroupId).distinct().toList())
                .stream().collect(Collectors.toMap(Group::getId, g -> g));
        return mappings.stream().collect(Collectors.groupingBy(UserGroup::getUserId,
                Collectors.mapping(m -> groupsMap.get(m.getGroupId()), Collectors.toList())));
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> getDropdown(BaseFilterRequest req) {
        return groupRepository.findDropdown(req);
    }

    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "dropdown_roles")
    public List<DropdownResponse> getDropdown() {
        return groupRepository.findDropdown();
    }

    @Transactional
    @CacheEvict(value = {"groups", "userGroups", "permissions", "userModules", "groupModules", "allModulesTree", "myProfile", "dropdown_roles"}, allEntries = true)
    public void createGroup(GroupRequest request, Long userId) {
        Group group = groupMapper.fromCreateRequest(request);
        group.setCreated(LocalDateTime.now()).setCreatedBy(userId);
        assignUsersAndPermissions(groupRepository.save(group).getId(), request);
    }

    @Transactional
    @CacheEvict(value = {"groups", "group", "userGroups", "permissions", "userModules", "groupModules", "allModulesTree", "myProfile", "dropdown_roles"}, allEntries = true)
    public void updateGroup(Long id, GroupRequest request, Long userId) {
        Group group = getGroupById(id);
        groupMapper.updateFromRequest(request, group);
        group.setModified(LocalDateTime.now()).setModifiedBy(userId);
        groupRepository.save(group);
        assignUsersAndPermissions(id, request);
    }

    @Transactional
    @CacheEvict(value = {"groups", "group", "userGroups", "permissions", "userModules", "groupModules", "allModulesTree", "myProfile", "dropdown_roles"}, allEntries = true)
    public void deleteGroup(Long id, Long userId) {
        Group group = getGroupById(id);
        group.setIsActive(2).setModified(LocalDateTime.now()).setModifiedBy(userId);
        groupRepository.save(group);
    }

    // * Cascade-reassign users and permissions (wipe-then-insert)
    private void assignUsersAndPermissions(Long groupId, GroupRequest request) {
        userGroupRepository.removeAllUsersFromGroup(groupId);
        if (request.getUserList() != null)
            request.getUserList().forEach(u -> userGroupRepository.assignUserToGroup(u.getUserId(), groupId));

        permissionRepository.removeAllPermissionsFromGroup(groupId);
        if (request.getModuleList() != null)
            request.getModuleList().forEach(m -> permissionRepository.assignPermission(groupId, m.getModuleId()));
    }

    @Transactional
    @CacheEvict(value = {"userGroups", "permissions", "userModules", "groupModules", "allModulesTree", "myProfile"}, allEntries = true)
    public void removeAllGroupsFromUser(Long userId) {
        userGroupRepository.removeAllGroupsFromUser(userId);
    }

    @Transactional
    @CacheEvict(value = {"userGroups", "permissions", "userModules", "groupModules", "allModulesTree", "myProfile"}, allEntries = true)
    public void assignUserToGroup(Long userId, Long groupId) {
        userGroupRepository.assignUserToGroup(userId, groupId);
    }
}
