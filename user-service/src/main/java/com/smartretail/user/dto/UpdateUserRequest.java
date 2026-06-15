package com.smartretail.user.dto;

import com.smartretail.user.entity.UserRole;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequest {

    private String name;

    @Email(message = "Valid email is required")
    private String email;

    private String password;

    private UserRole role;
}
