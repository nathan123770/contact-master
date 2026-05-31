package com.contactmaster.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {
    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 30) String username,
            @NotBlank @Size(min = 6, max = 40) String password,
            @Email String email
    ) {
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record ChangePasswordRequest(@NotBlank String oldPassword, @NotBlank @Size(min = 6, max = 40) String newPassword) {
    }

    public record AuthResponse(Long userId, String username, String email, String token) {
    }
}
