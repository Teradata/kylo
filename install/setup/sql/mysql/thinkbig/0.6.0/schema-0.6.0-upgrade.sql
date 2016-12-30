SET SQL_MODE='ALLOW_INVALID_DATES';
use thinkbig;
delimiter //

create procedure update_to_060()

begin

CREATE TABLE IF NOT EXISTS `KYLO_ALERT` (
  `id` binary(16) NOT NULL,
  `type` varchar(128) NOT NULL,
  `level` varchar(10) NOT NULL,
  `state` varchar(15) NOT NULL,
  `create_time` bigint,
  `description` varchar(255) DEFAULT NULL,
  `content` text DEFAULT NULL, 
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `KYLO_ALERT_CHANGE` (
  `alert_id` binary(16) NOT NULL,
  `state` varchar(15) NOT NULL,
  `change_time` bigint,
  `user` varchar(128) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `content` text DEFAULT NULL
) ENGINE=InnoDB;

END//

delimiter ;

-- Execute the procedure
call update_to_060();

-- Drop the procedure
drop procedure update_to_060;