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
CREATE PROCEDURE abandon_feed_jobs(in feed varchar(255), in exitMessage varchar(255), in user_name varchar(255), out res integer)
BEGIN

if user_name is null THEN
set user_name = 'dladmin';
end if;


INSERT INTO KYLO_ALERT_CHANGE(alert_id,state,change_time,user_name,description)
SELECT a.id, 'HANDLED', round(UNIX_TIMESTAMP(NOW(3)) * 1000), user_name, concat('Abandon all failed jobs for feed ',feed)
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
    SELECT feed from dual )
  AND e.STATUS = 'FAILED'
  AND ctx.KEY_NAME = 'Kylo Alert Id'
and a.id = uuid_to_bin(substr(ctx.STRING_VAL,1,instr(ctx.STRING_VAL,':')-1))
and a.state = 'UNHANDLED'
and a.type ='http://kylo.io/alert/job/failure';

UPDATE KYLO_ALERT a
set a.state = 'HANDLED'
where a.id in (SELECT uuid_to_bin(substr(STRING_VAL,1,instr(STRING_VAL,':')-1))
FROM BATCH_JOB_EXECUTION_CTX_VALS ctx
INNER JOIN BATCH_JOB_EXECUTION e on e.JOB_EXECUTION_ID = ctx.JOB_EXECUTION_ID
 INNER JOIN BATCH_JOB_INSTANCE
    ON BATCH_JOB_INSTANCE.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID
    WHERE BATCH_JOB_INSTANCE.JOB_NAME in ( SELECT checkFeed.NAME
    FROM FEED_CHECK_DATA_FEEDS c
    inner join FEED f on f.name = feed and c.FEED_ID = f.id
    inner join FEED checkFeed on checkFeed.id = c.CHECK_DATA_FEED_ID
    UNION
    SELECT feed from dual )
  AND e.STATUS = 'FAILED'
  AND ctx.KEY_NAME = 'Kylo Alert Id');


UPDATE BATCH_JOB_EXECUTION
  INNER JOIN BATCH_JOB_INSTANCE
    ON BATCH_JOB_INSTANCE.JOB_INSTANCE_ID = BATCH_JOB_EXECUTION.JOB_INSTANCE_ID
SET BATCH_JOB_EXECUTION.STATUS = 'ABANDONED',
  BATCH_JOB_EXECUTION.EXIT_MESSAGE = CONCAT_WS('\n', BATCH_JOB_EXECUTION.EXIT_MESSAGE, exitMessage)
WHERE BATCH_JOB_INSTANCE.JOB_NAME in ( SELECT checkFeed.NAME
    FROM FEED_CHECK_DATA_FEEDS c
    inner join FEED f on f.name = feed and c.FEED_ID = f.id
    inner join FEED checkFeed on checkFeed.id = c.CHECK_DATA_FEED_ID
    UNION
    SELECT feed from dual )
  AND BATCH_JOB_EXECUTION.STATUS = 'FAILED';

  --   need to return a value for this procedure calls to work on postgresql and with spring-data-jpa repositories and named queries
set res = 1;

END;