package com.contactmaster.service;

import com.contactmaster.dto.ContactDtos.ContactResponse;
import com.contactmaster.dto.DashboardDtos.Statistics;
import com.contactmaster.model.Contact;
import com.contactmaster.repository.ContactGroupRepository;
import com.contactmaster.repository.ContactRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private final ContactRepository contactRepository;
    private final ContactGroupRepository groupRepository;

    public DashboardService(ContactRepository contactRepository, ContactGroupRepository groupRepository) {
        this.contactRepository = contactRepository;
        this.groupRepository = groupRepository;
    }

    public Statistics statistics(Long userId) {
        long birthdayCount = birthdayContacts(userId).size();
        return new Statistics(
                contactRepository.countByUserIdAndDeletedFalse(userId),
                contactRepository.countByUserIdAndFavoriteTrueAndDeletedFalse(userId),
                groupRepository.findByUserIdOrderByCreatedAtAsc(userId).size(),
                contactRepository.countByUserIdAndDeletedTrue(userId),
                birthdayCount
        );
    }

    public List<ContactResponse> recent(Long userId) {
        return map(userId, contactRepository.findTop6ByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId));
    }

    public List<ContactResponse> favorites(Long userId) {
        return map(userId, contactRepository.findTop6ByUserIdAndFavoriteTrueAndDeletedFalseOrderByUpdatedAtDesc(userId));
    }

    public List<ContactResponse> birthdays(Long userId) {
        return map(userId, birthdayContacts(userId));
    }

    private List<Contact> birthdayContacts(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(7);
        return contactRepository.findByUserIdAndDeletedFalse(userId).stream()
                .filter(contact -> contact.getBirthday() != null)
                .filter(contact -> isWithinNextSevenDays(contact.getBirthday(), today, end))
                .sorted(Comparator.comparing(contact -> contact.getBirthday().withYear(today.getYear())))
                .toList();
    }

    private boolean isWithinNextSevenDays(LocalDate birthday, LocalDate today, LocalDate end) {
        LocalDate thisYear = birthday.withYear(today.getYear());
        LocalDate nextOccurrence = thisYear.isBefore(today) ? thisYear.plusYears(1) : thisYear;
        return !nextOccurrence.isBefore(today) && !nextOccurrence.isAfter(end);
    }

    private List<ContactResponse> map(Long userId, List<Contact> contacts) {
        Map<Long, String> groups = groupRepository.findByUserIdOrderByCreatedAtAsc(userId).stream()
                .collect(Collectors.toMap(group -> group.getId(), group -> group.getName()));
        return contacts.stream()
                .map(contact -> ContactResponse.from(contact, groups.get(contact.getGroupId())))
                .toList();
    }
}
