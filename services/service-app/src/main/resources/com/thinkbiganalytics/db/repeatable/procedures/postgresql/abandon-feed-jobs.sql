-- -
-- #%L
-- kylo-service-app
-- %%
-- Copyright (C) 2017 ThinkBig Analytics
-- %%
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--     http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- #L%
-- -
CREATE or replace function abandon_feed_jobs(in feed varchar(255), in exitMessage varchar(255), in user_name varchar(255))
  RETURNS integer AS $$
BEGIN

if user_name is null THEN
user_name := 'dladmin';
end if;


INSERT INTO KYLO_ALERT_CHANGE(alert_id,state,change_time,user_name,description)
SELECT a.id, 'HANDLED', round(EXTRACT (EPOCH FROM now()::timestamp)::float*1000), user_name, concat('Abandon all failed jobs for feed ',feed)
FROM BATCH_JOB_EXECUTION_CTX_VALS ctx
INNER JOIN BATCH_JOB_EXECUTION e on e.JOB_EXECUTION_ID = ctx.JOB_EXECUTION_ID
INNER JOIN BATCH_JOB_INSTANCE ON BATCH_JOB_INSTANCE.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID
INNER JOIN FEED f on f.id = BATCH_JOB_INSTANCE.FEED_ID
INNER JOIN KYLO_ALERT a on a.entity_id = f.id and a.entity_type = 'FEED'
    WHERE BATCH_JOB_INSTANCE.JOB_NAME in ( SELECT checkFeed.NAME
    FROM FEED_CHECK_DATA_FEEDS c
    inner join FEED f on f.name = feed and c.FEED_ID = f.id
    inner join FEED checkFeed on checkFeed.id = c.CHECK_DATA_FEED_ID
    UNION
    SELECT feed  )
  AND e.STATUS = 'FAILED'
  AND ctx.KEY_NAME = 'Kylo Alert Id'
and a.id = cast(substr(ctx.STRING_VAL,1,strpos(ctx.STRING_VAL,':')-1) as UUID)
and a.state = 'UNHANDLED'
and a.type ='http://kylo.io/alert/job/failure';

UPDATE KYLO_ALERT
set state = 'HANDLED'
where id in (SELECT cast(substr(STRING_VAL,1,strpos(STRING_VAL,':')-1) as UUID)
FROM BATCH_JOB_EXECUTION_CTX_VALS ctx
INNER JOIN BATCH_JOB_EXECUTION e on e.JOB_EXECUTION_ID = ctx.JOB_EXECUTION_ID
 INNER JOIN BATCH_JOB_INSTANCE
    ON BATCH_JOB_INSTANCE.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID
    WHERE BATCH_JOB_INSTANCE.JOB_NAME in ( SELECT checkFeed.NAME
    FROM FEED_CHECK_DATA_FEEDS c
    inner join FEED f on f.name = feed and c.FEED_ID = f.id
    inner join FEED checkFeed on checkFeed.id = c.CHECK_DATA_FEED_ID
    UNION
    SELECT feed)
  AND e.STATUS = 'FAILED'
  AND ctx.KEY_NAME = 'Kylo Alert Id');


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
