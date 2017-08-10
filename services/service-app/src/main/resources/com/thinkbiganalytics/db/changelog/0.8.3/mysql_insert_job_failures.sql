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

INSERT INTO KYLO_ALERT
(id,
type,
level,
state,
create_time,
description,
cleared,
content,
sub_type,
entity_type,
entity_id)
SELECT uuid_to_bin(uuid()), 'http://kylo.io/alert/job/failure','FATAL','UNHANDLED',e.LAST_UPDATED,CONCAT('Failed Job ',e.JOB_EXECUTION_ID,' for feed ',f.name),
'N',CONCAT('{"type":"java.lang.Long","value":"',e.JOB_EXECUTION_ID,'"}'),
'Entity','FEED',e.JOB_EXECUTION_ID
FROM BATCH_JOB_EXECUTION e
inner join BATCH_JOB_INSTANCE i on i.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID
inner join FEED f on f.id = i.FEED_ID
WHERE e.STATUS = 'FAILED';

INSERT INTO KYLO_ALERT_CHANGE(alert_id,state,change_time,user_name,description)
SELECT ID,state,create_time,'dladmin',description
FROM KYLO_ALERT a
where type = 'http://kylo.io/alert/job/failure'
and state = 'UNHANDLED'
and not exists (select alert_id from KYLO_ALERT_CHANGE where alert_id = a.ID );