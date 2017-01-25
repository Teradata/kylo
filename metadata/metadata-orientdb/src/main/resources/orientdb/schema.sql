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

CREATE CLASS IdentityVertex EXTENDS V;
CREATE PROPERTY IdentityVertex.id STRING;

CREATE CLASS Feed EXTENDS IdentityVertex;
CREATE PROPERTY Feed.name STRING;

CREATE CLASS FeedData EXTENDS IdentityVertex;
CREATE PROPERTY FeedData.name STRING;

CREATE CLASS DataSource EXTENDS FeedData;

CREATE CLASS DataDestination EXTENDS FeedData;

CREATE CLASS Dataset EXTENDS IdentityVertex;
CREATE PROPERTY Dataset.name STRING;

CREATE CLASS ChangeSet EXTENDS IdentityVertex;
CREATE PROPERTY ChangeSet.time DATETIME;

CREATE CLASS DataOperation EXTENDS IdentityVertex;
CREATE PROPERTY DataOperation.type STRING;
CREATE PROPERTY DataOperation.time DATETIME;
CREATE PROPERTY DataOperation.status INTEGER;


// FeedData Accesses Dataset
CREATE CLASS Accesses EXTENDS E;

// ChangeSet Changes Dataset
CREATE CLASS Changes EXTENDS E;

// DataOperation ResultsIn ChangeSet
CREATE CLASS ResultsIn EXTENDS E;

// DataDestination Produced DataOperation
CREATE CLASS Produced EXTENDS E;

// Feed ReadsFrom DataSource
CREATE CLASS ReadsFrom EXTENDS E;

// Feed WritesTo DataDestination
CREATE CLASS WritesTo EXTENDS E;



