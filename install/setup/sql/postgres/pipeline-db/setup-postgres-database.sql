CREATE DATABASE pipeline_db owner=pguser;
\connect pipeline_db;
CREATE CAST (varchar as bigint) with inout as implicit;