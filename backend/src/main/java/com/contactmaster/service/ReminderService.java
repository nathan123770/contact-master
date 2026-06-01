package com.contactmaster.service;

import com.contactmaster.common.BusinessException;
import com.contactmaster.dto.ReminderDtos.*;
import com.contactmaster.model.Contact;
import com.contactmaster.model.ContactReminder;
import com.contactmaster.repository.ContactReminderRepository;
import com.contactmaster.repository.ContactRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReminderService {
    private final ContactReminderRepository reminderRepository;
    private final ContactRepository contactRepository;

    public ReminderService(ContactReminderRepository reminderRepository, ContactRepository contactRepository) {
        this.reminderRepository = reminderRepository;
        this.contactRepository = contactRepository;
    }

    public Page<ReminderResponse> search(Long userId,
                                         String status,
                                         ReminderType type,
                                         String keyword,
                                         Long contactId,
                                         int page,
                                         int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.min(Math.max(size, 1), 100));
        Page<ContactReminder> reminders = reminderRepository.search(
                userId,
                normalizeStatus(status),
                type,
                normalize(keyword),
                contactId,
                LocalDate.now(),
                pageable
        );
        Map<Long, ContactSummary> contacts = contactSummaries(reminders.getContent());
        return reminders.map(reminder -> ReminderResponse.from(reminder, contacts.get(reminder.getContactId()), LocalDate.now()));
    }

    @Transactional
    public ReminderResponse create(Long userId, ReminderRequest request) {
        Contact contact = findActiveContact(userId, request.contactId());
        ContactReminder reminder = new ContactReminder();
        reminder.setUserId(userId);
        fill(reminder, request);
        ContactReminder saved = reminderRepository.save(reminder);
        return ReminderResponse.from(saved, ContactSummary.from(contact), LocalDate.now());
    }

    @Transactional
    public ReminderResponse update(Long userId, Long id, ReminderRequest request) {
        ContactReminder reminder = findOwned(userId, id);
        Contact contact = findActiveContact(userId, request.contactId());
        fill(reminder, request);
        return ReminderResponse.from(reminder, ContactSummary.from(contact), LocalDate.now());
    }

    @Transactional
    public ReminderResponse complete(Long userId, Long id, CompleteReminderRequest request) {
        ContactReminder reminder = findOwned(userId, id);
        reminder.setCompleted(request.completed());
        reminder.setCompletedAt(request.completed() ? LocalDateTime.now() : null);
        ContactSummary contact = contactSummaries(List.of(reminder)).get(reminder.getContactId());
        return ReminderResponse.from(reminder, contact, LocalDate.now());
    }

    @Transactional
    public void delete(Long userId, Long id) {
        reminderRepository.delete(findOwned(userId, id));
    }

    @Transactional
    public void deleteByContact(Long userId, Long contactId) {
        reminderRepository.deleteByUserIdAndContactId(userId, contactId);
    }

    public List<ReminderResponse> dashboard(Long userId) {
        LocalDate today = LocalDate.now();
        List<ContactReminder> reminders = dashboardReminderRows(userId, today);
        Map<Long, ContactSummary> contacts = activeContactSummaries(reminders);
        return reminders.stream()
                .filter(reminder -> contacts.containsKey(reminder.getContactId()))
                .map(reminder -> ReminderResponse.from(reminder, contacts.get(reminder.getContactId()), today))
                .limit(6)
                .toList();
    }

    public DashboardReminderSummary dashboardSummary(Long userId) {
        LocalDate today = LocalDate.now();
        List<ContactReminder> reminders = dashboardReminderRows(userId, today);
        Map<Long, ContactSummary> contacts = activeContactSummaries(reminders);
        long overdue = reminders.stream()
                .filter(reminder -> contacts.containsKey(reminder.getContactId()))
                .filter(reminder -> reminder.getRemindDate().isBefore(today))
                .count();
        long todayCount = reminders.stream()
                .filter(reminder -> contacts.containsKey(reminder.getContactId()))
                .filter(reminder -> reminder.getRemindDate().isEqual(today))
                .count();
        long upcoming = reminders.stream()
                .filter(reminder -> contacts.containsKey(reminder.getContactId()))
                .filter(reminder -> reminder.getRemindDate().isAfter(today))
                .count();
        return new DashboardReminderSummary(
                overdue,
                todayCount,
                upcoming
        );
    }

    private List<ContactReminder> dashboardReminderRows(Long userId, LocalDate today) {
        return reminderRepository.findByUserIdAndCompletedFalseAndRemindDateLessThanEqualOrderByRemindDateAscUpdatedAtDesc(
                userId,
                today.plusDays(7)
        );
    }

    private ContactReminder findOwned(Long userId, Long id) {
        return reminderRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException("提醒不存在"));
    }

    private Contact findActiveContact(Long userId, Long contactId) {
        Contact contact = contactRepository.findByIdAndUserId(contactId, userId)
                .orElseThrow(() -> new BusinessException("联系人不存在"));
        if (contact.isDeleted()) {
            throw new BusinessException("联系人已在回收站，不能创建提醒");
        }
        return contact;
    }

    private void fill(ContactReminder reminder, ReminderRequest request) {
        reminder.setContactId(request.contactId());
        reminder.setType(request.type());
        reminder.setRemindDate(request.remindDate());
        reminder.setNote(normalize(request.note()));
    }

    private Map<Long, ContactSummary> contactSummaries(List<ContactReminder> reminders) {
        List<Long> contactIds = reminders.stream()
                .map(ContactReminder::getContactId)
                .distinct()
                .toList();
        return contactRepository.findAllById(contactIds).stream()
                .collect(Collectors.toMap(Contact::getId, ContactSummary::from));
    }

    private Map<Long, ContactSummary> activeContactSummaries(List<ContactReminder> reminders) {
        List<Long> contactIds = reminders.stream()
                .map(ContactReminder::getContactId)
                .distinct()
                .toList();
        return contactRepository.findAllById(contactIds).stream()
                .filter(contact -> !contact.isDeleted())
                .collect(Collectors.toMap(Contact::getId, ContactSummary::from));
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeStatus(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
}
