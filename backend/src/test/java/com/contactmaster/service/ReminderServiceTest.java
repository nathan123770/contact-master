package com.contactmaster.service;

import com.contactmaster.common.BusinessException;
import com.contactmaster.dto.ReminderDtos.*;
import com.contactmaster.model.Contact;
import com.contactmaster.model.ContactReminder;
import com.contactmaster.repository.ContactReminderRepository;
import com.contactmaster.repository.ContactRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReminderServiceTest {
    private final ContactReminderRepository reminderRepository = mock(ContactReminderRepository.class);
    private final ContactRepository contactRepository = mock(ContactRepository.class);
    private final ReminderService service = new ReminderService(reminderRepository, contactRepository);

    @Test
    void createRejectsContactsOwnedByAnotherUser() {
        ReminderRequest request = new ReminderRequest(8L, ReminderType.FOLLOW_UP, null, LocalDateTime.now().plusDays(1), "Call back");
        when(contactRepository.findByIdAndUserId(8L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("联系人不存在");

        verify(reminderRepository, never()).save(any());
    }

    @Test
    void searchReturnsOnlyCurrentUsersActiveContactReminders() {
        ContactReminder reminder = reminder(1L, 2L, 3L, ReminderType.FOLLOW_UP, LocalDateTime.now().plusDays(1), false);
        when(reminderRepository.search(eq(1L), eq("pending"), isNull(), eq("zhang"), isNull(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(reminder), PageRequest.of(0, 10), 1));
        when(contactRepository.findAllById(List.of(3L))).thenReturn(List.of(contact(3L, 1L, "Zhang San", "13800138000", false)));

        Page<ReminderResponse> page = service.search(1L, "pending", null, " zhang ", null, 1, 10);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).contactName()).isEqualTo("Zhang San");
        verify(reminderRepository).search(eq(1L), eq("pending"), isNull(), eq("zhang"), isNull(), any(), any(), any(), any());
    }

    @Test
    void completeUpdatesCompletionTimestamp() {
        ContactReminder reminder = reminder(9L, 1L, 3L, ReminderType.OTHER, LocalDateTime.now(), false);
        when(reminderRepository.findByIdAndUserId(9L, 1L)).thenReturn(Optional.of(reminder));
        when(contactRepository.findAllById(List.of(3L))).thenReturn(List.of(contact(3L, 1L, "Li Si", "13900139000", false)));

        ReminderResponse response = service.complete(1L, 9L, new CompleteReminderRequest(true));

        assertThat(response.completed()).isTrue();
        assertThat(response.completedAt()).isNotNull();
    }

    @Test
    void deleteByContactRemovesAllRemindersForThatContactAndUser() {
        service.deleteByContact(1L, 3L);

        verify(reminderRepository).deleteByUserIdAndContactId(1L, 3L);
    }

    @Test
    void dashboardSummaryCountsOnlyActiveContacts() {
        LocalDateTime now = LocalDateTime.now();
        ContactReminder overdue = reminder(1L, 1L, 3L, ReminderType.FOLLOW_UP, now.minusMinutes(1), false);
        ContactReminder todayReminder = reminder(2L, 1L, 4L, ReminderType.OTHER, now.plusMinutes(30), false);
        ContactReminder deletedContactReminder = reminder(3L, 1L, 5L, ReminderType.BIRTHDAY, now.plusDays(1), false);
        when(reminderRepository.findByUserIdAndCompletedFalseAndRemindAtLessThanEqualOrderByRemindAtAscUpdatedAtDesc(eq(1L), any()))
                .thenReturn(List.of(overdue, todayReminder, deletedContactReminder));
        when(contactRepository.findAllById(List.of(3L, 4L, 5L)))
                .thenReturn(List.of(
                        contact(3L, 1L, "Zhang San", "13800138000", false),
                        contact(4L, 1L, "Li Si", "13900139000", false),
                        contact(5L, 1L, "Deleted", "13700137000", true)
                ));

        DashboardReminderSummary summary = service.dashboardSummary(1L);

        assertThat(summary.overdueCount()).isEqualTo(1);
        assertThat(summary.todayCount()).isEqualTo(1);
        assertThat(summary.upcomingCount()).isZero();
    }

    private Contact contact(Long id, Long userId, String name, String phone, boolean deleted) {
        Contact contact = new Contact();
        ReflectionTestUtils.setField(contact, "id", id);
        contact.setUserId(userId);
        contact.setName(name);
        contact.setPhone(phone);
        contact.setDeleted(deleted);
        return contact;
    }

    private ContactReminder reminder(Long id, Long userId, Long contactId, ReminderType type, LocalDateTime remindAt, boolean completed) {
        ContactReminder reminder = new ContactReminder();
        ReflectionTestUtils.setField(reminder, "id", id);
        reminder.setUserId(userId);
        reminder.setContactId(contactId);
        reminder.setType(type);
        reminder.setRemindAt(remindAt);
        reminder.setNote("note");
        reminder.setCompleted(completed);
        return reminder;
    }
}
