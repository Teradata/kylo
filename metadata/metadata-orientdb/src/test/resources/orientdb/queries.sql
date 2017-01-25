-- -
-- #%L
-- thinkbig-metadata-orientdb
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


select expand(out('ReadsFrom').out('Accesses').in('Accesses').in('WritesTo')) from Feed where name = 'Feed X'


select id, name from (select expand(out('ReadsFrom')) from Feed where name = 'Feed X')

select id, name from (select expand(out('ReadsFrom').out('Accesses').in('Accesses').in('WritesTo')) from Feed where name = 'Feed X')

select out('ReadsFrom') from Feed where name = 'Feed X'


select out('ReadsFrom').out('Accesses').in('Accesses').in('WritesTo') from Feed where name = 'Feed X';

select expand(out('ReadsFrom').out('Accesses').in('Accesses').in('WritesTo')) from Feed where name = 'Feed X';


select from ReadsFrom;
select from WritesTo;
select from Accesses;

select from Changes;
select from ResultsIn;
select from Produced;











