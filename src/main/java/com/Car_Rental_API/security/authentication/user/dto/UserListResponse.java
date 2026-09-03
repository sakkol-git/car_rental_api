package com.Car_Rental_API.security.authentication.user.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;
    private List<RoleSummary> roles;
    private String createdBy;
    private LocalDateTime created;
    private String modifiedBy;
    private LocalDateTime modified;
}
