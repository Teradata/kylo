CREATE DATABASE kylo owner=pguser;
\connect kylo;
CREATE CAST (varchar as bigint) with inout as implicit;