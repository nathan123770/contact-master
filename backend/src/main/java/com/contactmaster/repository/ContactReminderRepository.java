package com.contactmaster.repository;

import com.contactmaster.dto.ReminderDtos.ReminderType;
import com.contactmaster.model.ContactReminder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ContactReminderRepository extends JpaRepository<ContactReminder, Long> {
    Optional<ContactReminder> findByIdAndUserId(Long id, Long userId);

    @Query("""
            select r from ContactReminder r, Contact c
            where r.contactId = c.id
              and r.userId = :userId
              and c.userId = :userId
              and c.deleted = false
              and (:contactId is null or r.contactId = :contactId)
              and (:type is null or r.type = :type)
              and (
                :status is null or :status = '' or
                (:status = 'pending' and r.completed = false) or
                (:status = 'completed' and r.completed = true) or
                (:status = 'overdue' and r.completed = false and r.remindDate < :today) or
                (:status = 'today' and r.completed = false and r.remindDate = :today)
              )
              and (
                :keyword is null or :keyword = '' or
                lower(c.name) like lower(concat('%', :keyword, '%')) or
                lower(c.phone) like lower(concat('%', :keyword, '%')) or
                lower(coalesce(r.note, '')) like lower(concat('%', :keyword, '%'))
              )
            order by
              case when r.completed = false and r.remindDate < :today then 0 else 1 end,
              r.completed asc,
              r.remindDate asc,
              r.updatedAt desc
            """)
    Page<ContactReminder> search(@Param("userId") Long userId,
                                 @Param("status") String status,
                                 @Param("type") ReminderType type,
                                 @Param("keyword") String keyword,
                                 @Param("contactId") Long contactId,
                                 @Param("today") LocalDate today,
                                 Pageable pageable);

    List<ContactReminder> findByUserIdAndCompletedFalseAndRemindDateLessThanEqualOrderByRemindDateAscUpdatedAtDesc(Long userId, LocalDate endDate);

    @Modifying
    void deleteByUserIdAndContactId(Long userId, Long contactId);
}
