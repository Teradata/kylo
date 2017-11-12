CREATE  FUNCTION timestamp_nearest_hour(date_time TIMESTAMP) RETURNS timestamp
BEGIN

RETURN  DATE_FORMAT(DATE_ADD(date_time, INTERVAL (IF(MINUTE(date_time) < 30, 0, 60) - MINUTE(date_time)) MINUTE),'%Y-%m-%d %H:00:00');
END;