package com.Car_Rental_API.security.authentication.util;


import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final List<String> groups;
    private final boolean enabled;
    private final boolean partner;
    private final String fullName;
    private final String email;
    private final String phone;

    // * Standard system user constructor
    public CustomUserDetails(Long userId, String username, String password, List<String> groups, boolean enabled) {
        this(userId, username, password, groups, enabled, false, username, null, null);
    }

    // * Partner user / detailed constructor
    public CustomUserDetails(Long userId, String username, String password, List<String> groups, boolean enabled, boolean partner, String fullName, String email, String phone) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.groups = groups != null ? groups : Collections.emptyList();
        this.enabled = enabled;
        this.partner = partner;
        this.fullName = fullName != null ? fullName : username;
        this.email = email;
        this.phone = phone;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return groups.stream().map(g -> new SimpleGrantedAuthority("GROUP_" + g)).collect(Collectors.toList());
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
}


