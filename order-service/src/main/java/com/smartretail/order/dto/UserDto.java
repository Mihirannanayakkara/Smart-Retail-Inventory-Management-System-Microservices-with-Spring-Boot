package com.smartretail.order.dto;

import lombok.Data;

@Data
public class UserDto {
    private String id;
    private String name;
    private String email;
    private String role;
    private String status;
}
