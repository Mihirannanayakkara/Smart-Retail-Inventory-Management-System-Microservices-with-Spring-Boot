package com.smartretail.user.dto;

import com.smartretail.user.entity.UserRole;
import com.smartretail.user.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String name;
    private String email;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;
}
