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

DELETE EDGE Accesses;

DELETE EDGE Changes;

DELETE EDGE ResultsIn;

DELETE EDGE Produced;

DELETE EDGE ReadsFrom;

DELETE EDGE WritesTo;

DELETE VERTEX IdentityVertex;

DELETE VERTEX Feed;

DELETE VERTEX FeedData;

DELETE VERTEX DataSource;

DELETE VERTEX DataDestination;

DELETE VERTEX Dataset;

DELETE VERTEX ChangeSet;

DELETE VERTEX DataOperation;


DROP CLASS Feed;

DROP CLASS DataSource;

DROP CLASS DataDestination;

DROP CLASS Dataset;

DROP CLASS ChangeSet;

DROP CLASS DataOperation;

DROP CLASS Accesses;

DROP CLASS Changes;

DROP CLASS ResultsIn;

DROP CLASS Produced;

DROP CLASS ReadsFrom;

DROP CLASS WritesTo;

DROP CLASS FeedData;

DROP CLASS IdentityVertex;

