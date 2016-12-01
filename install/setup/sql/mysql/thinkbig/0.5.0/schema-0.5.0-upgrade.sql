SET SQL_MODE='ALLOW_INVALID_DATES';
use thinkbig;

CREATE TABLE IF NOT EXISTS `AUDIT_LOG` (
  `id` binary(16) NOT NULL,
  `create_time` timestamp,
  `user` varchar(100) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `log_type` varchar(45) DEFAULT NULL,
  `entiity_id` binary(16) DEFAULT NULL
 PRIMARY KEY (`id`)
 ) ENGINE=InnoDB;
