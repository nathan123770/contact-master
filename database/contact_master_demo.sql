CREATE DATABASE IF NOT EXISTS contact_master
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE contact_master;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS contact_reminders;
DROP TABLE IF EXISTS import_records;
DROP TABLE IF EXISTS contacts;
DROP TABLE IF EXISTS contact_groups;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(60) NOT NULL UNIQUE,
  password_hash VARCHAR(128) NOT NULL,
  email VARCHAR(120),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE contact_groups (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  name VARCHAR(40) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_contact_groups_user_name (user_id, name),
  INDEX idx_contact_groups_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE contacts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  group_id BIGINT,
  name VARCHAR(60) NOT NULL,
  phone VARCHAR(30) NOT NULL,
  email VARCHAR(120),
  company VARCHAR(100),
  position VARCHAR(80),
  address VARCHAR(200),
  birthday DATE,
  remark VARCHAR(500),
  avatar_data LONGTEXT,
  favorite BOOLEAN NOT NULL DEFAULT FALSE,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  deleted_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_contacts_user_deleted (user_id, deleted),
  INDEX idx_contacts_user_group (user_id, group_id),
  INDEX idx_contacts_user_phone (user_id, phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE import_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  file_name VARCHAR(255),
  success_count INT NOT NULL DEFAULT 0,
  failure_count INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_import_records_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE contact_reminders (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO users (id, username, password_hash, email, created_at) VALUES
(1, 'admin', '13e72d52f189f89cf5a7ec035ef993a4581d5338ac4442f39e19531ead1575dc', 'admin@contact-master.local', NOW());

INSERT INTO contact_groups (id, user_id, name, created_at) VALUES
(1, 1, '默认分组', NOW()),
(2, 1, '家人', NOW()),
(3, 1, '朋友', NOW()),
(4, 1, '同事', NOW());

INSERT INTO contacts
  (user_id, group_id, name, phone, email, company, position, address, birthday, remark, avatar_data, favorite, deleted, created_at, updated_at)
VALUES
(1,1,'Alex Chen','13926060001','seed01@contact-master.local','Starline Tech','Product Manager','100 Century Ave, Shanghai','1992-03-14','Key account, monthly follow-up','/avatars/seed-contacts/avatar-01.jpg',1,0,NOW(),NOW()),
(1,3,'Bella Lin','13926060002','seed02@contact-master.local','Cedar Studio','Visual Designer','88 Wensan Rd, Hangzhou','1995-07-22','Design partner contact','/avatars/seed-contacts/avatar-02.jpg',0,0,NOW(),NOW()),
(1,4,'Caleb Zhou','13926060003','seed03@contact-master.local','Blueprint Advisory','Client Consultant','66 Jianguo Rd, Beijing','1989-11-05','Consulting project contact','/avatars/seed-contacts/avatar-03.jpg',1,0,NOW(),NOW()),
(1,3,'Diana Zhao','13926060004','seed04@contact-master.local','Morning Education','Course Lead','21 Zhongshan Rd, Nanjing','1993-01-30','Course communication contact','/avatars/seed-contacts/avatar-04.jpg',0,0,NOW(),NOW()),
(1,4,'Ethan Sun','13926060005','seed05@contact-master.local','Sea Whale Logistics','Operations Specialist','9 Tech Park, Shenzhen','1991-05-18','Logistics coordination contact','/avatars/seed-contacts/avatar-05.jpg',0,0,NOW(),NOW()),
(1,4,'Fiona Li','13926060006','seed06@contact-master.local','Northstar Capital','Investment Manager','12 Zhujiang New Town, Guangzhou','1988-09-09','Investment business contact','/avatars/seed-contacts/avatar-06.jpg',1,0,NOW(),NOW()),
(1,3,'Grace Wu','13926060007','seed07@contact-master.local','Orange Media','Content Planner','28 Chunxi Rd, Chengdu','1996-12-03','Content partnership contact','/avatars/seed-contacts/avatar-07.jpg',0,0,NOW(),NOW()),
(1,4,'Henry Zheng','13926060008','seed08@contact-master.local','Pine Software','Backend Engineer','328 Xinghu St, Suzhou','1990-04-25','Technical integration contact','/avatars/seed-contacts/avatar-08.jpg',1,0,NOW(),NOW()),
(1,3,'Ivy Wang','13926060009','seed09@contact-master.local','Shanhai Health','Marketing Manager','44 Zhongnan Rd, Wuhan','1994-08-16','Marketing campaign contact','/avatars/seed-contacts/avatar-09.jpg',0,0,NOW(),NOW()),
(1,2,'Jack Xu','13926060010','seed10@contact-master.local','Ginkgo Bookstore','Store Manager','6 Jiefangbei, Chongqing','1997-06-01','Store operations contact','/avatars/seed-contacts/avatar-10.jpg',0,0,NOW(),NOW()),
(1,4,'Kevin He','13926060011','seed11@contact-master.local','Flying Deer Auto','Sales Lead','118 Nanjing Rd, Tianjin','1987-02-11','Sales priority contact','/avatars/seed-contacts/avatar-11.jpg',1,0,NOW(),NOW()),
(1,3,'Luna Guo','13926060012','seed12@contact-master.local','Lime Dining','Brand Manager','19 Hubin S Rd, Xiamen','1992-10-27','Brand partnership contact','/avatars/seed-contacts/avatar-12.jpg',0,0,NOW(),NOW()),
(1,4,'Mason Ma','13926060013','seed13@contact-master.local','Prism Data','Data Analyst','50 Gaoxin Rd, Xian','1995-03-08','Data analysis contact','/avatars/seed-contacts/avatar-13.jpg',0,0,NOW(),NOW()),
(1,4,'Nora Hu','13926060014','seed14@contact-master.local','White Tower Build','Project Manager','2 Lushan S Rd, Changsha','1986-07-19','Project delivery contact','/avatars/seed-contacts/avatar-14.jpg',1,0,NOW(),NOW()),
(1,3,'Owen Gao','13926060015','seed15@contact-master.local','Glimmer Charity','Volunteer Coordinator','31 Hong Kong Middle Rd, Qingdao','1998-11-13','Charity event contact','/avatars/seed-contacts/avatar-15.jpg',0,0,NOW(),NOW()),
(1,4,'Penny Luo','13926060016','seed16@contact-master.local','Frontier Games','Interaction Designer','27 Zhongguancun Ave, Beijing','1993-09-24','Product experience contact','/avatars/seed-contacts/avatar-16.jpg',0,0,NOW(),NOW()),
(1,3,'Quinn Liang','13926060017','seed17@contact-master.local','Forest Travel','Travel Consultant','5 Zhongshan S Rd, Guilin','1991-01-06','Travel business contact','/avatars/seed-contacts/avatar-17.jpg',1,0,NOW(),NOW()),
(1,4,'Ryan Song','13926060018','seed18@contact-master.local','Long Bridge Trade','Procurement Manager','99 Fuming Rd, Ningbo','1989-05-29','Procurement coordination contact','/avatars/seed-contacts/avatar-18.jpg',0,0,NOW(),NOW()),
(1,3,'Sophie Tang','13926060019','seed19@contact-master.local','Bamboo Photo','Photographer','17 Qingnian Rd, Kunming','1996-04-12','Photography partner contact','/avatars/seed-contacts/avatar-19.jpg',0,0,NOW(),NOW()),
(1,4,'Theo Xie','13926060020','seed20@contact-master.local','Particle Lab','Frontend Engineer','10 Science Ave, Hefei','1994-12-20','Frontend technical contact','/avatars/seed-contacts/avatar-20.jpg',1,0,NOW(),NOW()),
(1,3,'Uma Cao','13926060021','seed21@contact-master.local','Qingchuan Law','Legal Assistant','77 Quancheng Rd, Jinan','1992-02-26','Legal support contact','/avatars/seed-contacts/avatar-21.jpg',0,0,NOW(),NOW()),
(1,4,'Victor Ding','13926060022','seed22@contact-master.local','Hetian Agriculture','Regional Manager','60 Nongye Rd, Zhengzhou','1988-08-04','Regional business contact','/avatars/seed-contacts/avatar-22.jpg',0,0,NOW(),NOW()),
(1,3,'Wendy Ye','13926060023','seed23@contact-master.local','Ink Cloud Network','QA Engineer','158 Wusi Rd, Fuzhou','1997-10-10','QA coordination contact','/avatars/seed-contacts/avatar-23.jpg',0,0,NOW(),NOW()),
(1,2,'Xavier Jiang','13926060024','seed24@contact-master.local','Luming Music','Music Teacher','1 Xuefu Rd, Harbin','1990-06-17','Music course contact','/avatars/seed-contacts/avatar-24.jpg',1,0,NOW(),NOW()),
(1,4,'Yara Fan','13926060025','seed25@contact-master.local','Azalea Hotel','Room Manager','23 Yangming Rd, Nanchang','1987-12-31','Hotel business contact','/avatars/seed-contacts/avatar-25.jpg',0,0,NOW(),NOW()),
(1,3,'Zane Yao','13926060026','seed26@contact-master.local','Blue Whale Insurance','Claims Specialist','45 Renmin Rd, Dalian','1995-01-15','Insurance claim contact','/avatars/seed-contacts/avatar-26.jpg',0,0,NOW(),NOW()),
(1,4,'Ariel Pan','13926060027','seed27@contact-master.local','Qiming Manufacturing','Quality Engineer','188 Taihu Ave, Wuxi','1991-09-02','Quality management contact','/avatars/seed-contacts/avatar-27.jpg',1,0,NOW(),NOW()),
(1,4,'Blake Zou','13926060028','seed28@contact-master.local','Oasis Energy','Business Development','99 Changfeng St, Taiyuan','1993-05-07','Energy business contact','/avatars/seed-contacts/avatar-28.jpg',0,0,NOW(),NOW()),
(1,3,'Clara Qin','13926060029','seed29@contact-master.local','Knownew Press','Editor','25 Nanjing N St, Shenyang','1998-03-23','Publishing editor contact','/avatars/seed-contacts/avatar-29.jpg',0,0,NOW(),NOW()),
(1,2,'Derek Du','13926060030','seed30@contact-master.local','Redwood Medical','Medical Assistant','68 Beijing Rd, Guiyang','1990-11-28','Medical coordination contact','/avatars/seed-contacts/avatar-30.jpg',1,0,NOW(),NOW());

SET FOREIGN_KEY_CHECKS = 1;
