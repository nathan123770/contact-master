package com.contactmaster.dto;

import com.contactmaster.model.Contact;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ContactDtos {
    public record ContactRequest(
            Long groupId,
            @NotBlank String name,
            @NotBlank @Pattern(regexp = "^[0-9+\\- ]{6,20}$", message = "手机号格式不正确") String phone,
            @Email String email,
            String company,
            String position,
            String address,
            LocalDate birthday,
            String remark,
            boolean favorite
    ) {
    }

    public record ContactResponse(
            Long id,
            Long groupId,
            String groupName,
            String name,
            String phone,
            String email,
            String company,
            String position,
            String address,
            LocalDate birthday,
            String remark,
            boolean favorite,
            boolean deleted,
            LocalDateTime deletedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static ContactResponse from(Contact contact, String groupName) {
            return new ContactResponse(
                    contact.getId(),
                    contact.getGroupId(),
                    groupName,
                    contact.getName(),
                    contact.getPhone(),
                    contact.getEmail(),
                    contact.getCompany(),
                    contact.getPosition(),
                    contact.getAddress(),
                    contact.getBirthday(),
                    contact.getRemark(),
                    contact.isFavorite(),
                    contact.isDeleted(),
                    contact.getDeletedAt(),
                    contact.getCreatedAt(),
                    contact.getUpdatedAt()
            );
        }
    }

    public record BatchDeleteRequest(List<Long> ids) {
    }

    public record ImportFailure(int row, String reason, String raw) {
    }

    public record ImportResult(int successCount, int failureCount, List<ImportFailure> failures) {
    }
}
