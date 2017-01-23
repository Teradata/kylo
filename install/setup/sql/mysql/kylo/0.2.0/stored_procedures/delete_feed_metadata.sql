use kylo;
DELIMITER $$
CREATE DEFINER=`root`@`%` PROCEDURE `delete_feed_metadata`(in systemCategoryName varchar(255), in systemFeedName varchar(255))
BEGIN

DECLARE categoryId varchar(255);
DECLARE feedId varchar(255);
DECLARE recordExists int default 0;
DECLARE output VARCHAR(4000) default '';

SELECT COUNT(*) into recordExists
FROM CATEGORY WHERE NAME = systemCategoryName;

 IF(recordExists >0) THEN
    SELECT HEX(id) into categoryId from CATEGORY WHERE NAME = systemCategoryName;
    SELECT COUNT(*) into recordExists FROM FEED WHERE NAME = systemFeedName and hex(category_id) = categoryId;
    SELECT CONCAT(output,'\n','CATEGORY ID IS :',HEX(categoryId)) into output;

    IF(recordExists >0) THEN

        SELECT hex(id) into feedId FROM FEED WHERE NAME = systemFeedName and HEX(category_id) = categoryId;
        SELECT CONCAT(output,'\n','FEED ID IS :',HEX(feedId)) into output;
        DELETE FROM FM_FEED WHERE hex(id) = feedId;
        DELETE FROM FEED WHERE hex(id) = feedId;
        SELECT  CONCAT(output,'\n','SUCCESSFULLY REMOVED THE FEED METADATA ',systemFeedName) into output;

    ELSE
    SELECT CONCAT(output,'\n','UNABLE TO FIND FEED ',systemFeedName) into output;
    END IF;
 ELSE
    SELECT CONCAT(output,'\n','UNABLE TO FIND CATEGORY ',systemCategoryName) into output;
END IF;


SELECT output;

END$$
DELIMITER ;
