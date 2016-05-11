CREATE DATABASE thinkbig owner=pguser;
\connect thinkbig;
CREATE CAST (varchar as bigint) with inout as implicit;