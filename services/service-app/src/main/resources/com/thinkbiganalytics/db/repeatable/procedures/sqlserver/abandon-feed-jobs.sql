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

CREATE PROCEDURE [dbo].[abandon_feed_jobs](@feed varchar(255))
AS
UPDATE
	BJE
SET
	BJE.STATUS = 'ABANDONED',
	BJE.EXIT_MESSAGE = CONCAT('Job manually abandoned @ ', FORMAT(Getdate(), '%Y-%m-%d %T:%f', 'en-US'))
FROM
	BATCH_JOB_EXECUTION BJE
	INNER JOIN BATCH_JOB_INSTANCE ON BATCH_JOB_INSTANCE.JOB_INSTANCE_ID = BJE.JOB_INSTANCE_ID
WHERE
	BATCH_JOB_INSTANCE.JOB_NAME in ( SELECT checkFeed.NAME
				FROM FEED_CHECK_DATA_FEEDS c
				inner join FEED f on f.name = @feed and c.FEED_ID = f.id
				inner join FEED checkFeed on checkFeed.id = c.CHECK_DATA_FEED_ID
				UNION
				SELECT feed from dual )
  AND BATCH_JOB_EXECUTION.STATUS = 'FAILED';