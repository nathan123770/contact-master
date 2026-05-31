package com.contactmaster.repository;

import com.contactmaster.model.ImportRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportRecordRepository extends JpaRepository<ImportRecord, Long> {
}
