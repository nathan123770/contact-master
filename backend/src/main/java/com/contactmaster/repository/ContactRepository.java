package com.contactmaster.repository;

import com.contactmaster.model.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    Optional<Contact> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndPhoneAndDeletedFalse(Long userId, String phone);

    boolean existsByUserIdAndPhoneAndDeletedFalseAndIdNot(Long userId, String phone, Long id);

    long countByUserIdAndDeletedFalse(Long userId);

    long countByUserIdAndFavoriteTrueAndDeletedFalse(Long userId);

    long countByUserIdAndDeletedTrue(Long userId);

    long countByUserIdAndGroupIdAndDeletedFalse(Long userId, Long groupId);

    List<Contact> findTop6ByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId);

    List<Contact> findTop6ByUserIdAndFavoriteTrueAndDeletedFalseOrderByUpdatedAtDesc(Long userId);

    List<Contact> findByUserIdAndDeletedFalse(Long userId);

    Page<Contact> findByUserIdAndDeletedTrueOrderByDeletedAtDesc(Long userId, Pageable pageable);

    @Query("""
            select c from Contact c
            where c.userId = :userId
              and c.deleted = false
              and (:groupId is null or c.groupId = :groupId)
              and (:favorite is null or c.favorite = :favorite)
              and (
                :keyword is null or :keyword = '' or
                lower(c.name) like lower(concat('%', :keyword, '%')) or
                lower(c.phone) like lower(concat('%', :keyword, '%')) or
                lower(coalesce(c.email, '')) like lower(concat('%', :keyword, '%')) or
                lower(coalesce(c.company, '')) like lower(concat('%', :keyword, '%')) or
                lower(coalesce(c.position, '')) like lower(concat('%', :keyword, '%'))
              )
            order by c.favorite desc, c.updatedAt desc
            """)
    Page<Contact> search(@Param("userId") Long userId,
                         @Param("keyword") String keyword,
                         @Param("groupId") Long groupId,
                         @Param("favorite") Boolean favorite,
                         Pageable pageable);

    @Query("""
            select c from Contact c
            where c.userId = :userId
              and c.deleted = false
              and (:keyword is null or :keyword = '' or
                lower(c.name) like lower(concat('%', :keyword, '%')) or
                lower(c.phone) like lower(concat('%', :keyword, '%')) or
                lower(coalesce(c.email, '')) like lower(concat('%', :keyword, '%')) or
                lower(coalesce(c.company, '')) like lower(concat('%', :keyword, '%')) or
                lower(coalesce(c.position, '')) like lower(concat('%', :keyword, '%')))
            order by c.createdAt desc
            """)
    List<Contact> exportRows(@Param("userId") Long userId, @Param("keyword") String keyword);
}
