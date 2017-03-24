CREATE function abandon_feed_jobs(in feed varchar(255), in exitMessage varchar(255))
  RETURNS void AS $$
BEGIN

UPDATE BATCH_JOB_EXECUTION
SET
  BATCH_JOB_EXECUTION.STATUS = 'ABANDONED',
  BATCH_JOB_EXECUTION.EXIT_MESSAGE = CONCAT_WS('\n',BATCH_JOB_EXECUTION.EXIT_MESSAGE,exitMessage)
FROM BATCH_JOB_EXECUTION
INNER JOIN BATCH_JOB_INSTANCE ON BATCH_JOB_INSTANCE.JOB_INSTANCE_ID = BATCH_JOB_EXECUTION.JOB_INSTANCE_ID
WHERE BATCH_JOB_INSTANCE.JOB_NAME in ( SELECT checkFeed.NAME
                                       FROM FEED_CHECK_DATA_FEEDS c
                                         inner join FEED f on f.name = feed and c.FEED_ID = f.id
                                         inner join FEED checkFeed on checkFeed.id = c.CHECK_DATA_FEED_ID
                                       UNION
                                       SELECT feed)
      AND BATCH_JOB_EXECUTION.STATUS = 'FAILED';

END;
$$ LANGUAGE plpgsql;