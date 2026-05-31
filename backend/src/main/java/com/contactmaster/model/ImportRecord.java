package com.contactmaster.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "import_records")
public class ImportRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private int successCount;

    @Column(nullable = false)
    private int failureCount;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }
}
