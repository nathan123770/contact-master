CREATE DATABASE IF NOT EXISTS contact_master
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS contact_reminders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  contact_id BIGINT NOT NULL,
  type VARCHAR(24) NOT NULL,
  remind_date DATE NOT NULL,
  remind_at DATETIME NOT NULL,
  note VARCHAR(500),
  completed BOOLEAN NOT NULL DEFAULT FALSE,
  completed_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_contact_reminders_user_status_date (user_id, completed, remind_at),
  INDEX idx_contact_reminders_contact (user_id, contact_id)
);
