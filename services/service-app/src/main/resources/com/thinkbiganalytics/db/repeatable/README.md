# Repeatables

`repeatable` directory contains idempotent database scripts, i.e. scripts which can be ran over and over again without breaking database structure.

Such scripts or objects include views, procedures, functions, triggers. 

In Liquibase terms this means that each `changeSet` in this directory should have `runOnChange` property set to `true`. 

