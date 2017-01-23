use kylo;

/**
 View that maps a Check Data Feed to its corresponding Job Feed
 */
CREATE OR REPLACE VIEW CHECK_DATA_TO_FEED_VW as
SELECT FEED_ID FEED_ID, f2.NAME as FEED_NAME, check_feeds.CHECK_DATA_FEED_ID as KYLO_FEED_ID, f.NAME as KYLO_FEED_NAME
 FROM FEED_CHECK_DATA_FEEDS check_feeds
 INNER JOIN FEED f on f.ID = check_feeds.CHECK_DATA_FEED_ID
 INNER JOIN FEED f2 on f2.ID = check_feeds.FEED_ID
 WHERE f.FEED_TYPE = 'CHECK'
UNION ALL
SELECT ID,NAME,id, NAME from FEED
WHERE FEED_TYPE = 'FEED';


/**
Get the health of the feed merging the Check data job health into the correct feed for summarizing the counts
 */
CREATE OR REPLACE VIEW BATCH_FEED_SUMMARY_COUNTS_VW AS
SELECT f.FEED_ID as FEED_ID,f.FEED_NAME as FEED_NAME,
       count(e2.JOB_EXECUTION_ID) as ALL_COUNT,
       count(case when e2.status <>'ABANDONED' AND (e2.status = 'FAILED' or e2.EXIT_CODE = 'FAILED') then 1 else null end) as FAILED_COUNT,
       count(case when e2.status <>'ABANDONED' AND (e2.EXIT_CODE = 'COMPLETED') then 1 else null end) as COMPLETED_COUNT,
       count(case when e2.status = 'ABANDONED'then 1 else null end) as ABANDONED_COUNT,
        count(case when e2.status IN('STARTING','STARTED')then 1 else null end) as RUNNING_COUNT
FROM   BATCH_JOB_EXECUTION e2
INNER JOIN BATCH_JOB_INSTANCE i on i.JOB_INSTANCE_ID = e2.JOB_INSTANCE_ID
INNER JOIN CHECK_DATA_TO_FEED_VW f on f.KYLO_FEED_ID = i.FEED_ID
group by f.feed_id, f.feed_name;


/**
Get the feed and the last time it completed
 */
CREATE OR REPLACE  VIEW LATEST_FEED_JOB_END_TIME_VW AS
    SELECT f.id as FEED_ID, MAX(e.END_TIME) END_TIME
    FROM
       BATCH_JOB_EXECUTION e
       INNER JOIN BATCH_JOB_INSTANCE i on i.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID
       INNER JOIN FEED f on f.id = i.FEED_ID
       GROUP by f.id;


/**
Latest JOB EXECUTION grouped by Feed
 */
CREATE OR REPLACE VIEW LATEST_FEED_JOB_VW AS
          SELECT f.id as FEED_ID, MAX(e.JOB_EXECUTION_ID) JOB_EXECUTION_ID
    FROM
       BATCH_JOB_EXECUTION e
       INNER JOIN BATCH_JOB_INSTANCE i on i.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID
       INNER JOIN FEED f on f.id = i.FEED_ID
       GROUP by f.id;


/**
 get the feed and the latest job that has been finished
 */
CREATE OR REPLACE VIEW `kylo`.`LATEST_FINISHED_FEED_JOB_VW` AS
SELECT f.ID as FEED_ID,f.NAME as FEED_NAME,
       f.FEED_TYPE as FEED_TYPE,
       e.JOB_EXECUTION_ID as JOB_EXECUTION_ID,
       i.JOB_INSTANCE_ID as JOB_INSTANCE_ID,
       e.START_TIME,
       e.END_TIME,
       e.STATUS,
       e.EXIT_CODE,
       e.EXIT_MESSAGE
FROM   BATCH_JOB_EXECUTION e
INNER JOIN BATCH_JOB_INSTANCE i on i.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID
INNER JOIN FEED f on f.ID = i.FEED_ID
inner JOIN LATEST_FEED_JOB_END_TIME_VW maxJobs
                             on maxJobs.FEED_ID = f.ID
                             and maxJobs.END_TIME =e.END_TIME;



CREATE OR REPLACE VIEW FEED_HEALTH_VW AS
SELECT summary.FEED_ID as FEED_ID,
	   summary.FEED_NAME as FEED_NAME,
       e.JOB_EXECUTION_ID as JOB_EXECUTION_ID,
       i.JOB_INSTANCE_ID as JOB_INSTANCE_ID,
       e.START_TIME,
       e.END_TIME,
       e.STATUS,
       e.EXIT_CODE,
       e.EXIT_MESSAGE,
       summary.FAILED_COUNT,
       summary.COMPLETED_COUNT,
       summary.ABANDONED_COUNT,
       summary.ALL_COUNT,
       summary.RUNNING_COUNT
FROM   BATCH_JOB_EXECUTION e
INNER JOIN BATCH_JOB_INSTANCE i on i.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID
inner join BATCH_FEED_SUMMARY_COUNTS_VW summary on summary.FEED_ID = i.FEED_ID
inner JOIN LATEST_FEED_JOB_VW maxJobs
                             on maxJobs.FEED_ID = summary.FEED_ID
                             and maxJobs.JOB_EXECUTION_ID =e.JOB_EXECUTION_ID;
