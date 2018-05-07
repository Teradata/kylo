Install notes for running the 'generate-hdf-kylo-folder.sh'


**Upgrading**
 
1. Stop kylo
  * */opt/kylo/stop-kylo-apps.sh*
2. Backup original plugins and configuration
  * Backup /kylo-ui/plugins  and /kylo-services/plugins  (if you have custom plugins)
  * Backup /kylo-ui/conf/application.properties  and /kylo-services/conf/application.properties
3. Remove kylo
  * */opt/kylo/remove-kylo.sh*
4. Install kylo 
  * Download and install the rpm
5. Restore the previous Kylo Configuration. 
  * Copy back in the plugins (if you have any custom ones) from the backup folder
  * Copy back in the properties files to /ui and /services
6. Run the generate-hdf-kylo-folder.sh.  This will create a nifi-kylo.tar file with the artifacts needed for NiFi
  * */opt/kylo/nifi/hdf/generate-hdf-kylo-folder.sh*
7. Copy the generated nifi-kylo.tar to each NiFi Node and extract it
  * On Each NiFi node run the upgrade-nars-jars.sh script found in the tar file.  This will copy over the new nars/jars to the nifi location
  * *upgrade-nars-jars.sh /opt/nifi-kylo /usr/hdf/current/nifi nifi:nifi*  
8. Start Kylo
  - */opt/kylo/start-kylo-apps.sh*