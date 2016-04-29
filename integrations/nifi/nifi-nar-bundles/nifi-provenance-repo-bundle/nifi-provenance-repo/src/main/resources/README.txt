1. First create the Database thinkbig_nifi in MySQL if it is not already there
 >  CREATE DATABASE thinkbig_nifi;
2. Run the mysql-schema.sql scripts to create the event table and sequence table

3. go to your nifi.properites file and change the nifi.provenance.repository.implementation as shown below
   # Provenance Repository Properties
   ###nifi.provenance.repository.implementation=org.apache.nifi.provenance.PersistentProvenanceRepository
   nifi.provenance.repository.implementation=com.thinkbiganalytics.nifi.provenance.ThinkbigProvenanceEventRepository
4. Build with maven and then copy in the thinkbig-nifi-provenance.nar file to the lib folder

