package com.fitness.gateway.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponseDto {
    private String id;
    private String keycloakId;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
