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
 View that maps a Check Data Feed to its corresponding Job Feed
 */
CREATE OR REPLACE VIEW CHECK_DATA_TO_FEED_VW as
SELECT FEED_ID FEED_ID, f2.NAME as FEED_NAME, check_feeds.CHECK_DATA_FEED_ID as KYLO_FEED_ID, f.NAME as KYLO_FEED_NAME
 FROM FEED_CHECK_DATA_FEEDS check_feeds
 INNER JOIN FEED f on f.ID = check_feeds.CHECK_DATA_FEED_ID
 INNER JOIN FEED f2 on f2.ID = check_feeds.FEED_ID
 WHERE f.FEED_TYPE = 'CHECK'
UNION ALL
SELECT ID,NAME,id, NAME from FEED
WHERE FEED_TYPE = 'FEED';
