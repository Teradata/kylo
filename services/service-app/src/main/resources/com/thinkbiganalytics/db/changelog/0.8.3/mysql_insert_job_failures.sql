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

INSERT INTO `kylo`.`KYLO_ALERT`
(`id`,
`type`,
`level`,
`state`,
`create_time`,
`description`,
`cleared`,
`content`,
`sub_type`,
`entity_type`,
`entity_id`)
SELECT uuid_to_bin(uuid()), 'http://kylo.io/alert/job/failure','FATAL','UNHANDLED',e.LAST_UPDATED,CONCAT('Failed Job ',e.JOB_EXECUTION_ID,' for feed ',f.name),
'N',CONCAT('{"type":"java.lang.Long","value":"',e.JOB_EXECUTION_ID,'"}'),
'Entity','FEED',e.JOB_EXECUTION_ID
FROM BATCH_JOB_EXECUTION e
inner join BATCH_JOB_INSTANCE i on i.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID
inner join FEED f on f.id = i.FEED_ID
WHERE e.STATUS = 'FAILED'