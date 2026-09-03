package com.Car_Rental_API.security.authorization.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermissionChecker {

//    private final PermissionService permissionService;
//
    public boolean hasPermission(Authentication auth, String module, String action) {
//        if (auth == null || module == null || action == null) {
//            return false;
//        }
//
//        if (!(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
//            return false;
//        }
//
        return false;
    }
}