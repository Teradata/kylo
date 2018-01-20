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


CREATE PROCEDURE [dbo].[compact_feed_processor_stats]( @res VARCHAR OUTPUT )
AS

DECLARE @curr_date datetime = getdate();

DECLARE @output VARCHAR(255) = '';

DECLARE @insertRowCount INTEGER = 0;
DECLARE @deleteRowCount INTEGER = 0;
DECLARE @totalCompactSize INTEGER = 0;


INSERT INTO NIFI_FEED_PROCESSOR_STATS
SELECT fm_feed_name											 AS FM_FEED_NAME,
       NULL 											     AS nifi_processor_id,
       NULL  												 AS nifi_feed_process_group_id,
       MAX(COLLECTION_TIME) 											 AS COLLECTION_TIME,
       Sum(total_events)                                     AS TOTAL_EVENTS,
       Sum(duration_millis)                                  AS DURATION_MILLIS,
       Sum(bytes_in)                                         AS BYTES_IN,
       Sum(bytes_out)                                        AS BYTES_OUT,
       dateadd(hour, datediff(hour, 0, dateadd(mi, 30, MIN_EVENT_TIME)), 0)				 AS MIN_EVENT_TIME,
	     MAX(max_event_time) 				                     AS MAX_EVENT_TIME,
       Sum(jobs_started)                                     AS JOBS_STARTED,
       Sum(jobs_finished)                                    AS JOBS_FINISHED,
       Sum(jobs_failed)                                      AS JOBS_FAILED,
       Sum(processors_failed)                                AS PROCESSORS_FAILED,
       Sum(flow_files_started)                               AS FLOW_FILES_STARTED,
       Sum(flow_files_finished)                              AS FLOW_FILES_FINISHED,
       NULL                                                  AS COLLECTION_ID,
       SUM(collection_interval_sec)                          AS COLLECTION_INTERVAL_SEC,
       NEWID() 												 AS id,
       processor_name										 AS PROCESSOR_NAME,
       SUM(job_duration)                                     AS JOB_DURATION,
       SUM(successful_job_duration)                          AS SUCCESSFUL_JOB_DURATION,
       Min(cluster_node_id)                                  AS CLUSTER_NOD_ID,
       Min(cluster_node_address)                             AS CLUSTER_NOD_ADDRESS,
       Max(max_event_id)                                     AS MAX_EVENT_ID,
       Sum(failed_events)                                    AS FAILED_EVENTS,
       Max(latest_flow_file_id)                              AS LATEST_FLOW_FILE_ID,
       Max(error_messages)                                   AS ERROR_MESSAGES,
       Max(error_messages_timestamp)                         AS ERROR_MESSAGES_TIMESTAMP
FROM   NIFI_FEED_PROCESSOR_STATS
WHERE  collection_id is not null
AND    COLLECTION_TIME < dateadd(DAY,-1,@curr_date)   -- look for records processed before yesterday
GROUP  BY fm_feed_name,
          nifi_processor_id,
          processor_name,
          nifi_feed_process_group_id,
          dateadd(hour, datediff(hour, 0, dateadd(mi, 30, MIN_EVENT_TIME)), 0);

SET @insertRowCount =  @@ROWCOUNT;

DELETE FROM    NIFI_FEED_PROCESSOR_STATS
WHERE  collection_id is not null
AND    COLLECTION_TIME < dateadd(DAY,-1,@curr_date)   -- look for records processed before yesterday

SET @deleteRowCount =  @@ROWCOUNT;

SET @totalCompactSize = @deleteRowCount - @insertRowCount;

SET @output = CONCAT('Compacted ',@deleteRowCount,' into ',@insertRowCount,' grouping event time to nearest hour');


-- rollup data older than xx hours ago together, grouping every minute
-- keep collection_id so it can be rolled up later with daily rollup

INSERT INTO NIFI_FEED_PROCESSOR_STATS
SELECT fm_feed_name											 AS FM_FEED_NAME,
       NULL 											     AS nifi_processor_id,
       NULL  												 AS nifi_feed_process_group_id,
       MAX(COLLECTION_TIME) 											 AS COLLECTION_TIME,
       Sum(total_events)                                     AS TOTAL_EVENTS,
       Sum(duration_millis)                                  AS DURATION_MILLIS,
       Sum(bytes_in)                                         AS BYTES_IN,
       Sum(bytes_out)                                        AS BYTES_OUT,
       dateadd(minute, datediff(minute, 0, dateadd(ss, 30, MIN_EVENT_TIME)), 0)				 AS MIN_EVENT_TIME,
	   MAX(max_event_time) 				                     AS MAX_EVENT_TIME,
       Sum(jobs_started)                                     AS JOBS_STARTED,
       Sum(jobs_finished)                                    AS JOBS_FINISHED,
       Sum(jobs_failed)                                      AS JOBS_FAILED,
       Sum(processors_failed)                                AS PROCESSORS_FAILED,
       Sum(flow_files_started)                               AS FLOW_FILES_STARTED,
       Sum(flow_files_finished)                              AS FLOW_FILES_FINISHED,
       MAX(COLLECTION_ID)                                    AS COLLECTION_ID,
       SUM(collection_interval_sec)                          AS COLLECTION_INTERVAL_SEC,
       NEWID() 												 AS id,
       processor_name										 AS PROCESSOR_NAME,
       SUM(job_duration)                                     AS JOB_DURATION,
       SUM(successful_job_duration)                          AS SUCCESSFUL_JOB_DURATION,
       Min(cluster_node_id)                                  AS CLUSTER_NOD_ID,
       Min(cluster_node_address)                             AS CLUSTER_NOD_ADDRESS,
       Max(max_event_id)                                     AS MAX_EVENT_ID,
       Sum(failed_events)                                    AS FAILED_EVENTS,
       Max(latest_flow_file_id)                              AS LATEST_FLOW_FILE_ID,
       Max(error_messages)                                   AS ERROR_MESSAGES,
       Max(error_messages_timestamp)                         AS ERROR_MESSAGES_TIMESTAMP
FROM   NIFI_FEED_PROCESSOR_STATS
WHERE  collection_id is not null
AND    COLLECTION_TIME < dateadd(HOUR,-10,@curr_date) -- look for records processed 10 or more hours ago
GROUP  BY fm_feed_name,
          nifi_processor_id,
          processor_name,
          nifi_feed_process_group_id,
          dateadd(minute, datediff(minute, 0, dateadd(ss, 30, MIN_EVENT_TIME)), 0);

SET @insertRowCount = @@ROWCOUNT;

DELETE FROM    NIFI_FEED_PROCESSOR_STATS
WHERE  collection_id is not null
AND    COLLECTION_TIME < dateadd(HOUR,-10,@curr_date);  -- look for records processed 10 or more hours ago

SET @deleteRowCount = @@ROWCOUNT;

SET @totalCompactSize = @totalCompactSize + (@deleteRowCount - @insertRowCount);
SET @output = CONCAT(@output,'\n Compacted ',@deleteRowCount,' into ',@insertRowCount,' grouping event time to nearest minute');
SET @output = CONCAT(@output,'\n Reduced table by ',@totalCompactSize,' rows');

set @res = @output;

