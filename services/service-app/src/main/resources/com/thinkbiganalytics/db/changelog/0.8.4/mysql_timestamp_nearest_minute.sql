CREATE FUNCTION timestamp_nearest_minute(date_time TIMESTAMP) RETURNS timestamp
BEGIN

RETURN  DATE_FORMAT(DATE_ADD(date_time, INTERVAL IF(SECOND(date_time) < 30, 0, 1) MINUTE),'%Y-%m-%d %H:%i:00');
END;