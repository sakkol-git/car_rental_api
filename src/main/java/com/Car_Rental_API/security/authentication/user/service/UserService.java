package com.Car_Rental_API.security.authentication.user.service;


import java.time.LocalDateTime;
import java.util.List;

import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.exception.GlobalException;
import com.Car_Rental_API.common.util.QueryUtil;
import com.Car_Rental_API.security.authentication.auth.dto.ChangePasswordRequest;
import com.Car_Rental_API.security.authentication.user.dto.FilterUserRequest;
import com.Car_Rental_API.security.authentication.user.dto.UserListResponse;
import com.Car_Rental_API.security.authentication.user.dto.UserRequest;
import com.Car_Rental_API.security.authentication.user.dto.UserResponse;
import com.Car_Rental_API.security.authentication.user.mapper.UserMapper;
import com.Car_Rental_API.security.authentication.user.model.User;
import com.Car_Rental_API.security.authentication.user.repository.UserRepository;
import com.Car_Rental_API.security.authorization.permission.model.Module;
import com.Car_Rental_API.security.authorization.permission.model.ModuleType;
import com.Car_Rental_API.security.authorization.permission.repository.ModuleTypeRepository;
import com.Car_Rental_API.security.authorization.permission.service.PermissionService;
import com.Car_Rental_API.security.authorization.role.service.GroupService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final ModuleTypeRepository moduleTypeRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final GroupService groupService;

    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "users", key = "#req != null ? #req.toString() : 'default'")
    public PageResult<UserListResponse> getAllUsers(FilterUserRequest req) {
        FilterUserRequest filter = req != null ? req : new FilterUserRequest();
        List<User> users = userRepository.findAll(filter);
        long total = QueryUtil.shouldCount(filter.getPage(), () -> userRepository.countAll(filter));

        // * Bulk-fetch roles to avoid N+1 queries
        List<Long> userIds = users.stream().map(User::getId).toList();
        var userGroups = groupService.getUserGroupsBulk(userIds);

        List<UserListResponse> list = users.stream().map(u -> {
            UserListResponse res = userMapper.toListItem(u);
            res.setRoles(userMapper.toRoleResponses(userGroups.getOrDefault(u.getId(), List.of())));
            return res;
        }).toList();

        return new PageResult<>(list, total);
    }

    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "user", key = "#id")
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new GlobalException("User not found", 404));
    }

    // * Get Enriched User Response by ID
    public UserResponse getUserResponseById(Long id) {
        User user = getUserById(id);
        UserResponse res = userMapper.toResponse(user);
        res.setRoles(userMapper.toRoleResponses(groupService.getUserGroups(id)));
        return res;
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> getDropdown(BaseFilterRequest req) {
        return userRepository.findDropdown(req);
    }

    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "dropdown_users")
    public List<DropdownResponse> getDropdown() {
        return userRepository.findDropdown();
    }

    @Transactional
    @CacheEvict(value = {"users", "dropdown_users"}, allEntries = true)
    public void createUser(UserRequest req, Long userId) {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) throw new GlobalException("Username exists", 400);

        User user = userMapper.fromCreateRequest(req);
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setCreatedBy(userId);
        User saved = userRepository.save(user, true);

        if (req.getGroupIds() != null) req.getGroupIds().forEach(gid -> groupService.assignUserToGroup(saved.getId(), gid));
    }

    @Transactional
    @CacheEvict(value = {"users", "user", "myProfile", "dropdown_users"}, allEntries = true)
    public void updateUser(Long id, UserRequest req, Long userId) {
        User user = getUserById(id);
        userMapper.updateUserFromRequest(req, user);

        boolean updatePassword = false;
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
            updatePassword = true;
        }

        user.setModifiedBy(userId);
        userRepository.save(user, updatePassword);

        // * Sync groups if provided
        if (req.getGroupIds() != null) {
            groupService.removeAllGroupsFromUser(id);
            req.getGroupIds().forEach(gid -> groupService.assignUserToGroup(id, gid));
        }
    }

    @Transactional
    @CacheEvict(value = {"users", "user", "permissions", "userModules", "userGroups", "myProfile", "dropdown_users"}, allEntries = true)
    public void deleteUser(Long id, Long userId) {
        User user = getUserById(id);
        user.setIsActive(2);
        user.setModified(LocalDateTime.now());
        user.setModifiedBy(userId);
        userRepository.save(user, false);
    }

    @Transactional
    @CacheEvict(value = {"user", "myProfile"}, key = "#userId")
    public void updateUserSession(Long userId, String sid, LocalDateTime start) {
        userRepository.updateSessionInfo(userId, sid, start);
    }

    @Cacheable(value = "myProfile", key = "#userId")
    public UserResponse getMyProfile(Long userId) {
        User user = getUserById(userId);
        UserResponse res = userMapper.toResponse(user);
        res.setRoles(userMapper.toRoleResponses(groupService.getUserGroups(userId)));

        List<Module> modules = permissionService.getUserModules(userId);
        List<ModuleType> types = moduleTypeRepository.findAll();
        userMapper.enrichModulesForProfile(types, modules);
        res.setModuleTypeList(types.stream().filter(t -> !t.getModuleList().isEmpty()).toList());
        return res;
    }

    @Transactional
    @CacheEvict(value = {"user", "myProfile"}, key = "#id")
    public void changePassword(Long id, ChangePasswordRequest req) {
        User user = getUserById(id);
        if (req.getOldPassword() != null && !passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new GlobalException("Incorrect old password", 400);
        }
        userRepository.updatePassword(id, passwordEncoder.encode(req.getNewPassword()));
    }

}

