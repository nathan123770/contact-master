package com.contactmaster.repository;

import com.contactmaster.model.ContactGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContactGroupRepository extends JpaRepository<ContactGroup, Long> {
    List<ContactGroup> findByUserIdOrderByCreatedAtAsc(Long userId);

    Optional<ContactGroup> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndName(Long userId, String name);
}
