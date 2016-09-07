echo "Building Package"
mvn clean install package  -DskipTests

echo "Copy Jar To Nifi Lib"
cp /root/user/shashi/pcng/git/data-lake-accelerator/integrations/nifi/nifi-nar-bundles/nifi-authorization/nifi-authorization-nar/target/nifi-authorization-nar-0.3.0-SNAPSHOT.nar /opt/nifi/current/lib/

chown nifi:root /opt/nifi/current/lib/nifi-authorization-nar-0.3.0-SNAPSHOT.nar

echo "Restart Nifi with Nifi User"
su -c "/opt/nifi/current/bin/nifi.sh restart" -s /bin/sh nifi  


echo "Clear Previouse Data for Registeration"


hadoop fs -rmr /model.db/customers/ranger1
hadoop fs -rmr /etl/customers/ranger1
hadoop fs -rmr /app/warehouse/customers/ranger1


hive -e "drop table customers.ranger1;drop table customers.ranger1_feed;drop table customers.ranger1_invalid;drop table customers.ranger1_profile;drop table customers.ranger1_valid; "


