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
update DATABASECHANGELOG
set MD5SUM = '7:c19df976038f3f162a9db91f3ded270e'
where FILENAME like '%com/thinkbiganalytics/db/changelog/0.8.2/alter-feed-add-stream-column.xml';

update DATABASECHANGELOG
set MD5SUM = '7:aa784981473e0adbde6b4b13a47b80bc'
where FILENAME like '%com/thinkbiganalytics/db/changelog/0.8.2/alter-nifi-feed-processor-stats.xml';

update DATABASECHANGELOG
set MD5SUM = '7:7d8b9d92dd39cde6a432afd4908ac569'
where FILENAME like '%com/thinkbiganalytics/db/changelog/0.8.2/alter-job_execution-add-stream-column.xml';

update DATABASECHANGELOG
set MD5SUM = '7:ab5cdf176b6711ea6583bea5dc38fff2'
where FILENAME like '%com/thinkbiganalytics/db/changelog/0.8.2/alter_feed_add_time_btwn_batch_jobs_column.xml';

update DATABASECHANGELOG
set MD5SUM = '7:c364c09e39467f6746c4ed426c654d7e'
where FILENAME like '%com/thinkbiganalytics/db/changelog/0.8.2/alter_batch_job_execution_ctx_vals_table.xml';

