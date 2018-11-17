# Intro
Assumes you are setting up Livy in a VirtualBox sandbox.
Assumes you have a termimal open on your laptop with the current directory where this LIVY_INSTALL.md document resides

Livy can be downloaded from this website (not a download link, web page for a list of mirrors)
https://www.apache.org/dyn/closer.lua/incubator/livy/0.5.0-incubating/livy-0.5.0-incubating-bin.zip

Or, just run this wget on the mirror link given:

    wget http://www-us.apache.org/dist/incubator/livy/0.5.0-incubating/livy-0.5.0-incubating-bin.zip

# Working with Livy
Livy launches a session when you visit the Visual Query page.  You have to watch the Livy UI (port 8998) and wait for 
the session to complete starting before clicking through to the wrangler, via "Continue to Step 2" button.
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

## On sandbox

    ln -s /opt/kylo/kylo-services/plugin/app/kylo-spark-shell-client-v1-0.10.0-SNAPSHOT.jar repl_2.10-jars/
    ln -s /opt/kylo/kylo-services/plugin/app/kylo-spark-shell-client-v2-0.10.0-SNAPSHOT.jar repl_2.11-jars/
    bin/livy-server (runs in foreground of terminal/writes logs to stdout)
    bin/livy-server start (runs in background/writes logs to log dir)

### copy some properties to spark.properties

    cp /opt/kylo/kylo-services/conf/spark.properties  /opt/kylo/kylo-services/conf/spark.properties.bak
    cat <<EOF >> /opt/kylo/kylo-services/conf/spark.properties
    spark.livy.hostname=sandbox.kylo.io
    spark.livy.port=8998
    EOF

    service kylo-services start

# Steps to change the shell

## Use LIVY
    
    service kylo-services stop
    cp /opt/kylo/setup/plugins/kylo-spark-livy-server-0.10.0-SNAPSHOT.jar /opt/kylo/kylo-services/plugin/
    sed -i.bak 's/kylo-shell/kylo-livy/' /opt/kylo/kylo-services/conf/application.properties
    service kylo-services start

## Use Spark Shell

    service kylo-services stop
    sed -i.bak 's/kylo-livy/kylo-shell/' /opt/kylo/kylo-services/conf/application.properties
    service kylo-services start