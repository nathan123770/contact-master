package com.contactmaster.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class GroupDtos {
    public record GroupRequest(@NotBlank String name) {
    }

    public record GroupResponse(Long id, String name, long contactCount, LocalDateTime createdAt) {
    }
}
