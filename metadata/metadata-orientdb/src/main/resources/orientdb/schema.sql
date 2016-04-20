
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



