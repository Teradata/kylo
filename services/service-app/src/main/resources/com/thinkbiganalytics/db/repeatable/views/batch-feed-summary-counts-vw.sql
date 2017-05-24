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
