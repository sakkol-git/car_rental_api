package com.Car_Rental_API.security.authorization.permission.service;

import com.Car_Rental_API.common.exception.GlobalException;
import com.Car_Rental_API.security.authorization.permission.dto.ModuleRequest;
import com.Car_Rental_API.security.authorization.permission.model.Module;
import com.Car_Rental_API.security.authorization.permission.model.ModuleType;
import com.Car_Rental_API.security.authorization.permission.model.Permission;
import com.Car_Rental_API.security.authorization.permission.repository.ModuleRepository;
import com.Car_Rental_API.security.authorization.permission.repository.ModuleTypeRepository;
import com.Car_Rental_API.security.authorization.permission.repository.PermissionRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final ModuleRepository moduleRepository;
    private final ModuleTypeRepository moduleTypeRepository;

    // * Check module + action permission
    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "permissions", key = "#userId + '-' + #moduleName + '-' + (#action != null ? #action : '')")
    public boolean checkUserHasPermission(Long userId, String moduleName, String action) {
        if (moduleName == null) return false;

        // * Strategy 1: Find module matching exact name and type (e.g. name="Vehicle", type="Public To App")
        Optional<Module> moduleOpt = moduleRepository.findByNameAndType(moduleName, action);

        // * Strategy 2: Check combined "Module (Action)" pattern (e.g. "User (view)")
        if (moduleOpt.isEmpty() && action != null && !action.isEmpty()) {
            String combinedName = moduleName + " (" + action + ")";
            moduleOpt = moduleRepository.findByName(combinedName);
        }

        // * Strategy 3: Fallback to base module name
        if (moduleOpt.isEmpty()) {
            moduleOpt = moduleRepository.findByName(moduleName);
        }

        return moduleOpt.map(module -> permissionRepository.userHasModuleAccess(userId, module.getId())).orElse(false);
    }

    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "userModules", key = "#userId")
    public List<Module> getUserModules(Long userId) {
        return permissionRepository.findModuleIdsByUserId(userId).stream().map(moduleRepository::findById).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "groupModules", key = "#groupId")
    public List<Long> getGroupModuleIds(Long groupId) {
        return permissionRepository.findByGroupId(groupId).stream().map(Permission::getModuleId).collect(Collectors.toList());
    }

    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "allModulesTree", key = "(#userId != null ? #userId : 0) + '-' + (#groupId != null ? #groupId : 0)")
    public List<ModuleType> getAllModulesTree(Long userId, Long groupId) {
        List<ModuleType> moduleTypes = moduleTypeRepository.findAll();
        List<Module> allModules = moduleRepository.findAll();

        // * Get assigned IDs if context provided
        java.util.Set<Long> assignedIds = new java.util.HashSet<>();
        boolean filterEnabled = false;

        if (userId != null) {
            assignedIds.addAll(permissionRepository.findModuleIdsByUserId(userId));
            filterEnabled = true;
        } else if (groupId != null) {
            assignedIds.addAll(permissionRepository.findByGroupId(groupId).stream().map(Permission::getModuleId).collect(Collectors.toSet()));
            filterEnabled = true;
        }

        for (ModuleType type : moduleTypes) {
            boolean finalFilterEnabled = filterEnabled;
            List<Module> children = allModules.stream().filter(m -> java.util.Objects.equals(m.getModuleTypeId(), type.getId())).map(m -> {
                m.setChecked(assignedIds.contains(m.getId()));
                return m;
            }).filter(m -> !finalFilterEnabled || Boolean.TRUE.equals(m.getChecked())).collect(Collectors.toList());
            type.setModuleList(children);
        }

        // * Only return Types that have modules
        return moduleTypes.stream().filter(t -> t.getModuleList() != null && !t.getModuleList().isEmpty()).collect(Collectors.toList());
    }

    // * Create Module Set (ModuleType + child Modules)
    @Transactional
    @CacheEvict(value = {"permissions", "userModules", "groupModules", "allModulesTree", "myProfile", "user"}, allEntries = true)
    public void createModuleSet(ModuleRequest request) {
        // * 1. Ensure doesn't already exist
        if (moduleTypeRepository.findByName(request.getName()).isPresent()) {
            throw new GlobalException("Module already exists", 400);
        }

        // * 2. Create Parent Type
        Long typeId = moduleTypeRepository.save(ModuleType.builder().name(request.getName()).ordering(0).build());

        // * 3. Create Child Modules
        if (request.getModules() != null) {
            for (int i = 0; i < request.getModules().size(); i++) {
                ModuleRequest.ModuleDetail m = request.getModules().get(i);
                moduleRepository.save(Module.builder().moduleTypeId(typeId).name(m.getName()).type(m.getType()).ordering(i).build());
            }
        }
    }

    // * Update Module Set (ModuleType + child Modules)
    @Transactional
    @CacheEvict(value = {"permissions", "userModules", "groupModules", "allModulesTree", "myProfile", "user"}, allEntries = true)
    public void updateModuleSet(Long module_type_id, ModuleRequest request) {
        // * 1. Check parent type exists
        ModuleType existingType = moduleTypeRepository.findById(module_type_id)
                .orElseThrow(() -> new GlobalException("Module type not found", 404));

        // * 2. If name changed, check uniqueness
        if (!existingType.getName().equalsIgnoreCase(request.getName())) {
            if (moduleTypeRepository.findByName(request.getName()).isPresent()) {
                throw new GlobalException("Module already exists", 400);
            }
            moduleTypeRepository.update(module_type_id, request.getName());
        }

        // * 3. Process child modules
        List<Module> currentModules = moduleRepository.findByModule_type_id(module_type_id);
        List<ModuleRequest.ModuleDetail> incomingModules = request.getModules() != null ? request.getModules() : List.of();

        Set<Long> incomingIds = incomingModules.stream()
                .map(ModuleRequest.ModuleDetail::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // * Delete removed child modules & their permissions
        for (Module existingModule : currentModules) {
            if (!incomingIds.contains(existingModule.getId())) {
                permissionRepository.deleteByModuleId(existingModule.getId());
                moduleRepository.deleteById(existingModule.getId());
            }
        }

        // * Upsert incoming child modules
        for (int i = 0; i < incomingModules.size(); i++) {
            ModuleRequest.ModuleDetail detail = incomingModules.get(i);
            if (detail.getId() != null) {
                moduleRepository.update(Module.builder()
                        .id(detail.getId())
                        .moduleTypeId(module_type_id)
                        .name(detail.getName())
                        .type(detail.getType())
                        .ordering(i)
                        .build());
            } else {
                moduleRepository.save(Module.builder()
                        .moduleTypeId(module_type_id)
                        .name(detail.getName())
                        .type(detail.getType())
                        .ordering(i)
                        .build());
            }
        }
    }

    // * Delete Module Set (Recursive)
    @Transactional
    @CacheEvict(value = {"permissions", "userModules", "groupModules", "allModulesTree", "myProfile", "user"}, allEntries = true)
    public void deleteModuleSet(Long module_type_id) {
        // * 1. Delete associated permissions via module list
        moduleRepository.findByModule_type_id(module_type_id).forEach(m -> permissionRepository.deleteByModuleId(m.getId()));

        // * 2. Delete modules
        moduleRepository.deleteByModule_type_id(module_type_id);

        // * 3. Delete type
        moduleTypeRepository.deleteById(module_type_id);
    }

    // * Batch Reset All Module Sets (Truncate and recreate full module tree from JSON, then assign User 1 & Group 1)
    @Transactional
    @CacheEvict(value = {"permissions", "userModules", "groupModules", "allModulesTree", "myProfile", "user"}, allEntries = true)
    public void resetAllModuleSets(List<ModuleRequest> requests) {
        permissionRepository.truncateAllModuleData();

        if (requests != null) {
            for (ModuleRequest req : requests) {
                Long typeId = moduleTypeRepository.save(ModuleType.builder().name(req.getName()).ordering(0).build());
                if (req.getModules() != null) {
                    for (int i = 0; i < req.getModules().size(); i++) {
                        ModuleRequest.ModuleDetail m = req.getModules().get(i);
                        moduleRepository.save(Module.builder()
                                .moduleTypeId(typeId)
                                .name(m.getName())
                                .type(m.getType())
                                .ordering(i)
                                .build());
                    }
                }
            }
        }

        // * Automatically assign User ID 1 to Super Admin Role (Group 1) and grant ALL permissions
        permissionRepository.assignAllModulesToSuperAdminGroup();
    }
}

