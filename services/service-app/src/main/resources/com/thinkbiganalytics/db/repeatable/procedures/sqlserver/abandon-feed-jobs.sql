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

CREATE PROCEDURE [dbo].[abandon_feed_jobs](@feed varchar(255), @exitMessage varchar(255), @username varchar(255), @res integer OUTPUT)
AS

IF (@username IS NULL)
  SET @username = 'dladmin';


INSERT INTO KYLO_ALERT_CHANGE(alert_id,state,change_time,user_name,description)
SELECT a.id, 'HANDLED', FLOOR((CAST(GetUTCDate() as float) - 25567.0) * 86400000.0), @username, concat('Abandon all failed jobs for feed ', @feed)
FROM BATCH_JOB_EXECUTION_CTX_VALS ctx
INNER JOIN BATCH_JOB_EXECUTION e on e.JOB_EXECUTION_ID = ctx.JOB_EXECUTION_ID
INNER JOIN BATCH_JOB_INSTANCE ON BATCH_JOB_INSTANCE.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID
INNER JOIN FEED f on f.id = BATCH_JOB_INSTANCE.FEED_ID
INNER JOIN KYLO_ALERT a on a.entity_id = f.id and a.entity_type = 'FEED'
  WHERE BATCH_JOB_INSTANCE.JOB_NAME in (
    SELECT checkFeed.NAME
    FROM FEED_CHECK_DATA_FEEDS c
    INNER JOIN FEED f on f.name = @feed and c.FEED_ID = f.id
    INNER JOIN FEED checkFeed on checkFeed.id = c.CHECK_DATA_FEED_ID
    UNION
    SELECT @feed
  )
  AND e.STATUS = 'FAILED'
  AND ctx.KEY_NAME = 'Kylo Alert Id'
and a.id = CONVERT(BINARY(16), REPLACE(SUBSTRING(ctx.STRING_VAL, 1, CHARINDEX(':', ctx.STRING_VAL) - 1), '-', ''), 2)
and a.state = 'UNHANDLED'
and a.type ='http://kylo.io/alert/job/failure';

UPDATE KYLO_ALERT
SET state = 'HANDLED'
WHERE id in (
  SELECT CONVERT(BINARY(16), REPLACE(SUBSTRING(ctx.STRING_VAL, 1, CHARINDEX(':', ctx.STRING_VAL) - 1), '-', ''), 2)
  FROM BATCH_JOB_EXECUTION_CTX_VALS ctx
  INNER JOIN BATCH_JOB_EXECUTION e on e.JOB_EXECUTION_ID = ctx.JOB_EXECUTION_ID
  INNER JOIN BATCH_JOB_INSTANCE
    ON BATCH_JOB_INSTANCE.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID
  WHERE BATCH_JOB_INSTANCE.JOB_NAME in (
    SELECT checkFeed.NAME
    FROM FEED_CHECK_DATA_FEEDS c
    inner join FEED f on f.name = @feed and c.FEED_ID = f.id
    inner join FEED checkFeed on checkFeed.id = c.CHECK_DATA_FEED_ID
    UNION
    SELECT @feed
  )
  AND e.STATUS = 'FAILED'
  AND ctx.KEY_NAME = 'Kylo Alert Id'
);


UPDATE BATCH_JOB_EXECUTION
SET
  STATUS = 'ABANDONED',
  EXIT_MESSAGE = CONCAT_WS(CHAR(10),EXIT_MESSAGE,@exitMessage)
FROM BATCH_JOB_INSTANCE
WHERE BATCH_JOB_INSTANCE.JOB_INSTANCE_ID = BATCH_JOB_EXECUTION.JOB_INSTANCE_ID
AND BATCH_JOB_INSTANCE.JOB_NAME in (
  SELECT checkFeed.NAME
  FROM FEED_CHECK_DATA_FEEDS c
  inner join FEED f on f.name = @feed and c.FEED_ID = f.id
  inner join FEED checkFeed on checkFeed.id = c.CHECK_DATA_FEED_ID
  UNION
  SELECT @feed
)
AND STATUS = 'FAILED';

--   need to return a value for this procedure calls to work on postgresql and with spring-data-jpa repositories and named queries
set @res = 1;
