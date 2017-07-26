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
CREATE OR REPLACE VIEW FEED_HEALTH_VW AS
SELECT
    summary.FEED_ID AS FEED_ID,
    summary.FEED_NAME AS FEED_NAME,
    maxJobs.IS_STREAM AS IS_STREAM,
    e.JOB_EXECUTION_ID AS JOB_EXECUTION_ID,
    i.JOB_INSTANCE_ID AS JOB_INSTANCE_ID,
    e.START_TIME AS START_TIME,
    e.END_TIME AS END_TIME,
    e.STATUS AS STATUS,
    e.EXIT_CODE AS EXIT_CODE,
    e.EXIT_MESSAGE AS EXIT_MESSAGE,
    summary.FAILED_COUNT AS FAILED_COUNT,
    summary.COMPLETED_COUNT AS COMPLETED_COUNT,
    summary.ABANDONED_COUNT AS ABANDONED_COUNT,
    summary.ALL_COUNT AS ALL_COUNT,
    summary.RUNNING_COUNT AS RUNNING_COUNT
    FROM
    (((BATCH_JOB_EXECUTION e
    JOIN BATCH_JOB_INSTANCE i ON ((i.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID)))
    JOIN BATCH_FEED_SUMMARY_COUNTS_VW summary ON ((summary.FEED_ID = i.FEED_ID)))
    JOIN LATEST_FEED_JOB_VW maxJobs ON (((maxJobs.FEED_ID = summary.FEED_ID)
    AND (maxJobs.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID))));
