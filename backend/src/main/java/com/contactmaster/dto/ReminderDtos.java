package com.contactmaster.dto;

import com.contactmaster.model.ContactReminder;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReminderDtos {
    public enum ReminderType {
        BIRTHDAY,
        FOLLOW_UP,
        ANNIVERSARY,
        OTHER
    }

    public record ReminderRequest(
            @NotNull Long contactId,
            @NotNull ReminderType type,
            LocalDate remindDate,
            @NotNull LocalDateTime remindAt,
            String note
    ) {
    }

    public record CompleteReminderRequest(boolean completed) {
    }

    public record ReminderResponse(
            Long id,
            Long contactId,
            String contactName,
            String contactPhone,
            ReminderType type,
            LocalDate remindDate,
            LocalDateTime remindAt,
            String note,
            boolean completed,
            LocalDateTime completedAt,
            boolean overdue,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static ReminderResponse from(ContactReminder reminder, ContactSummary contact, LocalDateTime now) {
            boolean overdue = !reminder.isCompleted() && reminder.getRemindAt().isBefore(now);
            return new ReminderResponse(
                    reminder.getId(),
                    reminder.getContactId(),
                    contact == null ? null : contact.name(),
                    contact == null ? null : contact.phone(),
                    reminder.getType(),
                    reminder.getRemindDate(),
                    reminder.getRemindAt(),
                    reminder.getNote(),
                    reminder.isCompleted(),
                    reminder.getCompletedAt(),
                    overdue,
                    reminder.getCreatedAt(),
                    reminder.getUpdatedAt()
            );
        }
    }

    public record ContactSummary(Long id, String name, String phone) {
        public static ContactSummary from(com.contactmaster.model.Contact contact) {
            return new ContactSummary(contact.getId(), contact.getName(), contact.getPhone());
        }
    }

    public record DashboardReminderSummary(long overdueCount, long todayCount, long upcomingCount) {
    }

    public record DashboardReminders(DashboardReminderSummary summary, java.util.List<ReminderResponse> items) {
    }
}
