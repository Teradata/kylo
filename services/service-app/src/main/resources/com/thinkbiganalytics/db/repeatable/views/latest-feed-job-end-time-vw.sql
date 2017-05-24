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
Get the feed and the last time it completed
 */
CREATE OR REPLACE  VIEW LATEST_FEED_JOB_END_TIME_VW AS
    SELECT f.id as FEED_ID, MAX(e.END_TIME) END_TIME
    FROM
       BATCH_JOB_EXECUTION e
       INNER JOIN BATCH_JOB_INSTANCE i on i.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID
       INNER JOIN FEED f on f.id = i.FEED_ID
       GROUP by f.id;
