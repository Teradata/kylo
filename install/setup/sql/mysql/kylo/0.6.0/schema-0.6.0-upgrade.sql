SET SQL_MODE='ALLOW_INVALID_DATES';
use kylo;
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
  `cleared` varchar(1) DEFAULT NULL,
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


IF NOT EXISTS(SELECT table_name
            FROM INFORMATION_SCHEMA.COLUMNS
           WHERE table_schema = 'kylo'
             AND table_name = 'NIFI_FEED_PROCESSOR_STATS' and column_name = 'CLUSTER_NODE_ID') THEN

ALTER TABLE `NIFI_FEED_PROCESSOR_STATS`
ADD COLUMN `CLUSTER_NODE_ID` VARCHAR(255) NULL,
ADD COLUMN `CLUSTER_NODE_ADDRESS` VARCHAR(255) NULL,
ADD COLUMN `MAX_EVENT_ID` BIGINT(20) NULL;

END IF;


IF NOT EXISTS(SELECT table_name
            FROM INFORMATION_SCHEMA.COLUMNS
           WHERE table_schema = 'kylo'
             AND table_name = 'NIFI_EVENT' and column_name = 'CLUSTER_NODE_ID') THEN

ALTER TABLE `NIFI_EVENT`
ADD COLUMN `CLUSTER_NODE_ID` VARCHAR(255) NULL,
ADD COLUMN `CLUSTER_NODE_ADDRESS` VARCHAR(255) NULL;
END IF;



END//


delimiter ;

-- Execute the procedure
call update_to_060();

-- Drop the procedure
drop procedure update_to_060;