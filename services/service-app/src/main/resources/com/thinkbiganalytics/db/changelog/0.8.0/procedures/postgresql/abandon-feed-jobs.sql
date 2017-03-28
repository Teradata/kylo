CREATE FUNCTION abandon_feed_jobs(in feed varchar(255), in exitMessage varchar(255))
  RETURNS integer AS $$
BEGIN

UPDATE BATCH_JOB_EXECUTION
SET
  STATUS = 'ABANDONED',
  EXIT_MESSAGE = CONCAT_WS(chr(10),EXIT_MESSAGE,exitMessage)
FROM BATCH_JOB_INSTANCE
WHERE BATCH_JOB_INSTANCE.JOB_INSTANCE_ID = BATCH_JOB_EXECUTION.JOB_INSTANCE_ID
AND BATCH_JOB_INSTANCE.JOB_NAME in ( SELECT checkFeed.NAME
                                       FROM FEED_CHECK_DATA_FEEDS c
                                         inner join FEED f on f.name = feed and c.FEED_ID = f.id
                                         inner join FEED checkFeed on checkFeed.id = c.CHECK_DATA_FEED_ID
                                       UNION
                                       SELECT feed)
      AND STATUS = 'FAILED';

--   need to return a value for this procedure calls to work with spring-data-jpa repositories and named queries
return 1;

END;
$$ LANGUAGE plpgsql;