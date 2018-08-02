# Intro
Assumes you are setting up Livy in a VirtualBox sandbox.
Assumes you have a termimal open on your laptop with the current directory where this LIVY_INSTALL.md document resides


Livy can be downloaded from this website (not a download link, web page for a list of mirrors)
https://www.apache.org/dyn/closer.lua/incubator/livy/0.5.0-incubating/livy-0.5.0-incubating-bin.zip

# Working with Livy
Livy launches a session when you visit the Visual Query page.  You have to watch the Livy UI (port 8998) and wait for the session to complete starting before clicking through to the wrangler, via "Continue to Step 2" button.
This issue will be addressed by TBK-85

# Steps

## On sandbox
service kylo-services stop
cd $HOME
wget http://www-eu.apache.org/dist/incubator/livy/0.5.0-incubating/livy-0.5.0-incubating-bin.zip
unzip livy-0.5.0-incubating-bin.zip 
cd livy-0.5.0-incubating-bin
mkdir logs

## On laptop
scp -P 2222 l* 'root@localhost:$HOME/livy-0.5.0-incubating-bin/conf'

vim
## On sandbox
ln -s /opt/kylo/setup/plugins/spark-shell/app/kylo-spark-shell-client-v1-0.9.2-SNAPSHOT.jar repl_2.10-jars/
bin/livy-server (runs in foreground of terminal)
bin/livy-server start (runs in background and writes logs to log dir)

### copy some properties to spark.properties
cp /opt/kylo/kylo-services/conf/spark.properties  /opt/kylo/kylo-services/conf/spark.properties.bak
cat <<EOF >> /opt/kylo/kylo-services/conf/spark.properties
spark.livy.hostname=sandbox.kylo.io
spark.livy.port=8998
EOF

service kylo-services start
