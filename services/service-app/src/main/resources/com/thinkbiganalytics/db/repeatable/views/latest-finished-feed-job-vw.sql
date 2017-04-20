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

/**
 get the feed and the latest job that has been finished
 */
CREATE OR REPLACE VIEW LATEST_FINISHED_FEED_JOB_VW AS
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
