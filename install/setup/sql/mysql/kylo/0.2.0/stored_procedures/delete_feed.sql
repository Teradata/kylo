use kylo;
DELIMITER $$
CREATE DEFINER=`root`@`%` PROCEDURE `delete_feed`(in category varchar(255), in feed varchar(255))
BEGIN

CALL delete_feed_jobs(category,feed);
CALL delete_feed_metadata(category,feed);

END$$
DELIMITER ;
