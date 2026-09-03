package com.Car_Rental_API.security.authentication.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    private String username;
    private String password;
    private List<Long> groupIds;
    private String fullName;
    private String firstName;
    private String lastName;
    private String photo;
    private String signature;

}
