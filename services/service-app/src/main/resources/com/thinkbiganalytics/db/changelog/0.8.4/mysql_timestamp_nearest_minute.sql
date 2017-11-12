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

CREATE FUNCTION timestamp_nearest_minute(date_time TIMESTAMP) RETURNS timestamp
BEGIN

RETURN  DATE_FORMAT(DATE_ADD(date_time, INTERVAL IF(SECOND(date_time) < 30, 0, 1) MINUTE),'%Y-%m-%d %H:%i:00');
END;