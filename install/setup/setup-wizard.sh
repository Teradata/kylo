#!/bin/bash

OFFLINE=false
if [ $# -ge 1 ]
then
    if [ "$1" = "-o" ] || [ "$1" = "-O" ]
    then
        echo "Working in OFFLINE mode"
        OFFLINE=true
    fi
fi

CURRENT_DIR=$(dirname $0)
echo "The working directory is $CURRENT_DIR"

echo "Welcome to the Kylo setup wizard. Lets get started !!!"
echo " "
echo "Please enter Y/y or N/n to the following questions:"
echo " "

yes_no="^[yYnN]{1}$"
# (1) prompt user, and read command line argument

read -p "Enter the kylo home folder location, hit Enter for '/opt/kylo': " kylo_home_folder

if [[ -z "$kylo_home_folder" ]]; then
    kylo_home_folder=/opt/kylo
fi

echo " ";
while [[ ! $install_db =~ $yes_no ]]; do
    read -p "Would you like to install the database scripts in a database instance? Please enter y/n: " install_db
done

if [ "$install_db" == "y"  ] || [ "$install_db" == "Y" ] ; then

    while [[ ! $install_db_liquibase =~ $yes_no ]]; do
        read -p "Would you like Kylo to manage installing and upgrading the database automatically? Please enter y/n: " install_db_liquibase
    done

    if [ "$install_db_liquibase" == "n"  ] || [ "$install_db_liquibase" == "N" ] ; then
        echo " ";
        echo "OK. Disabling Liquibase in application.properties. Please see the Kylo documentation to see how to generate the database scripts"
        sed -i "s|liquibase.enabled=true|liquibase.enabled=false|" $kylo_home_folder/kylo-services/conf/application.properties

        echo " ";
        read -n 1 -s -r -p "Press any key to continue: " continue_from_liquibase


    fi

    # Only supporting MySql at this time
    database_type=0
    while [[ ! ${database_type} =~ ^[1-3]{1}$ ]]; do
        echo "Which database (Enter the number)?"
        echo "1) MySQL"
        echo "2) PostgresSQL"
        echo "3) SQL Server"
        read -p "> " database_type;
    done

    while : ; do

        echo
        echo "Please enter the database hostname or IP, hit Enter for 'localhost'";
        read -p "> " hostname;
        echo "Please enter the database ADMIN username";
        read -p "> " username;
        echo "Please enter the database ADMIN password";
        read -p "> " -s password;

        if [[ -z "$hostname" ]]; then
            hostname=localhost
        fi

        if [ "$database_type" == "1"  ] ; then
            echo "Creating MySQL database 'kylo'"
            ! mysql -h ${hostname} -u "${username}" --password="${password}" -e "create database if not exists kylo character set utf8 collate utf8_general_ci;" || break;
        fi
        if [ "$database_type" == "2"  ] ; then
            echo "Creating PostgreSQL database 'kylo'"
            ! PGPASSWORD="${password}" createdb -U kylo -h ${hostname} -E UTF8 -e kylo || break;
        fi
        if [ "$database_type" == "3"  ] ; then
            echo "Creating SQL Server database 'kylo'"
            hash sqlcmd 2>/dev/null || { echo >&2 "The required tool sqlcmd is not present in PATH. Aborting."; exit 1; }
            echo "If applicable, enter the Azure edition options including brackets, eg. (EDITION='basic'), otherwise leave blank"
            read -p "> " azure_options;
            ! sqlcmd -S ${hostname} -U "${username}" -P "${password}" -Q "CREATE DATABASE kylo ${azure_options}" || break;
        fi
    done
fi
echo " ";

USERS_FILE_CREATED=false;
if [ ! -f $kylo_home_folder/users.properties ]; then
    while true; do
        echo "Please enter the password for the dladmin user";
        read -p "> " -s DLADMIN_PASSWORD_1;
        printf "\n"
        echo "Please re-enter the password for the dladmin user";
        read -p "> " -s DLADMIN_PASSWORD_2;
        [ "$DLADMIN_PASSWORD_1" = "$DLADMIN_PASSWORD_2" ] && break
        printf "\n\n"
        echo "Passwords do not match. Please try again"
        printf "\n\n"
    done

    echo "dladmin=$DLADMIN_PASSWORD_1" >> $kylo_home_folder/users.properties
    sed -i "s|#security.auth.file.users=file:\/\/\/opt\/kylo\/users.properties|security.auth.file.users=file:\/\/$kylo_home_folder\/users.properties|" $kylo_home_folder/kylo-services/conf/application.properties
    sed -i "s|#security.auth.file.users=file:\/\/\/opt\/kylo\/users.properties|security.auth.file.users=file:\/\/$kylo_home_folder\/users.properties|" $kylo_home_folder/kylo-ui/conf/application.properties
    USERS_FILE_CREATED=true

    if [ ! -f $kylo_home_folder/groups.properties ]; then
        touch $kylo_home_folder/groups.properties
        sed -i "s|#security.auth.file.users=file:\/\/\/opt\/kylo\/groups.properties|security.auth.file.users=file:\/\/$kylo_home_folder\/groups.properties|" $kylo_home_folder/kylo-services/conf/application.properties
        sed -i "s|#security.auth.file.users=file:\/\/\/opt\/kylo\/groups.properties|security.auth.file.users=file:\/\/$kylo_home_folder\/groups.properties|" $kylo_home_folder/kylo-ui/conf/application.properties
    fi
fi

echo " ";
while [[ ! $java_type =~ ^[1-4]{1}$ ]]; do
    echo "Please choose an option to configure Java for Kylo, ActiveMQ, and NiFi"
    echo "1) I already have Java 8 or higher installed as the system Java and want to use that"
    echo "2) Install Java 8 in the /opt/java folder for me and use that one"
    echo "3) I have Java 8 or higher installed in another location already. I will provide the location"
    echo "4) Java is already setup. No changes necessary"
    read -p "> " java_type;
done

if [ "$java_type" == "3" ] ; then
    read -p "Please enter the location to the JAVA_HOME: " java_home
fi
echo " ";
while [[ ! $install_es =~ $yes_no ]]; do
    read -p "Would you like me to install a local elasticsearch instance? Please enter y/n: " install_es
done

echo " ";
while [[ ! $install_activemq =~ $yes_no ]]; do
    read -p "Would you like me to install a local activemq instance?  Please enter y/n: " install_activemq

    if [ "$install_activemq" == "y"  ] || [ "$install_activemq" == "Y" ] ; then
        read -p "Enter the Activemq home folder location, hit Enter for '/opt/activemq': " activemq_home

        if [[ -z "$activemq_home" ]]; then
            activemq_home=/opt/activemq
        fi

        read -p "Enter the user Activemq should run as, hit Enter for 'activemq': " activemq_user

        if [[ -z "$activemq_user" ]]; then
            activemq_user=activemq
        fi

        read -p "Enter the linux group Activemq should run as, hit Enter for 'activemq': " activemq_group

        if [[ -z "$activemq_group" ]]; then
            activemq_group=activemq
        fi
    fi

done

echo " ";
while [[ ! $install_nifi =~ $yes_no ]]; do
    read -p "Would you like me to install a local nifi instance? Please enter y/n: " install_nifi

    if [ "$install_nifi" == "y"  ] || [ "$install_nifi" == "Y" ] ; then
        read -p "Enter Nifi version you wish to install, hit Enter for '1.3.0': " nifi_version
        if [[ -z "$nifi_version" ]]; then
            nifi_version=1.3.0
        fi

        read -p "Enter the NiFi home folder location, hit Enter for '/opt/nifi': " nifi_home
        if [[ -z "$nifi_home" ]]; then
            nifi_home=/opt/nifi
        fi

        read -p "Enter the user NiFi should run as, hit Enter for 'nifi': " nifi_user
        if [[ -z "$nifi_user" ]]; then
            nifi_user=nifi
        fi

        read -p "Enter the linux group NiFi should run as, hit Enter for 'nifi': " nifi_group
        if [[ -z "$nifi_group" ]]; then
            nifi_group=nifi
        fi
    fi

done

if [ $OFFLINE = true ]
then
    cd $CURRENT_DIR
else
    cd $kylo_home_folder/setup
fi

if [ "$java_type" == "1" ] ; then
    echo "Using system Java 8 and remove the JAVA_HOME variable from kylo-ui and kylo-services"
    ./java/remove-default-kylo-java-home.sh $kylo_home_folder
elif [ "$java_type" == "2" ] ; then
    if [ $OFFLINE = true ]
    then
        ./java/install-java8.sh $kylo_home_folder $CURRENT_DIR -O
    else
        ./java/install-java8.sh $kylo_home_folder
    fi

elif [ "$java_type" == "3" ] ; then
    ./java/remove-default-kylo-java-home.sh $kylo_home_folder
    ./java/change-kylo-java-home.sh $java_home $kylo_home_folder
fi

if [ "$install_es" == "y"  ] || [ "$install_es" == "Y" ] ; then
    echo "Installing Elasticsearch"
        ES_JAVA_HOME="SYSTEM_JAVA"
    if [ "$java_type" == "2" ] ; then
        ES_JAVA_HOME=/opt/java/current
    elif [ "$java_type" == "3" ] ; then
        ES_JAVA_HOME=$java_home
    fi
    if [ $OFFLINE = true ]
    then
        ./elasticsearch/install-elasticsearch.sh $CURRENT_DIR $ES_JAVA_HOME -O
    else
        ./elasticsearch/install-elasticsearch.sh $kylo_home_folder/setup $ES_JAVA_HOME
    fi
fi

if [ "$install_activemq" == "y"  ] || [ "$install_activemq" == "Y" ] ; then
    echo "installing ActiveMQ"
    ACTIVEMQ_JAVA_HOME="SYSTEM_JAVA"
    if [ "$java_type" == "2" ] ; then
        ACTIVEMQ_JAVA_HOME=/opt/java/current
    elif [ "$java_type" == "3" ] ; then
        ACTIVEMQ_JAVA_HOME=$java_home
    fi

    if [ $OFFLINE = true ]
    then
        ./activemq/install-activemq.sh $activemq_home $activemq_user $activemq_group $ACTIVEMQ_JAVA_HOME $CURRENT_DIR -O
    else
        ./activemq/install-activemq.sh $activemq_home $activemq_user $activemq_group $ACTIVEMQ_JAVA_HOME
    fi

fi

if [ "$install_nifi" == "y"  ] || [ "$install_nifi" == "Y" ] ; then
    if [ $OFFLINE = true ]
    then
        ./nifi/install-nifi.sh $nifi_version $nifi_home $nifi_user $nifi_group $CURRENT_DIR -O
    else
        ./nifi/install-nifi.sh $nifi_version $nifi_home $nifi_user $nifi_group
    fi


    if [ "$java_type" == "2" ] ; then
        ./java/change-nifi-java-home.sh /opt/java/current $nifi_home/current
    elif  [ "$java_type" == "3" ] ; then
        ./java/change-nifi-java-home.sh $java_home $nifi_home/current
    fi

    if [ $OFFLINE = true ]
    then
        ./nifi/install-kylo-components.sh $nifi_home $kylo_home_folder $nifi_user $nifi_group $CURRENT_DIR -O
    else
        ./nifi/install-kylo-components.sh $nifi_home $kylo_home_folder $nifi_user $nifi_group
    fi



fi

if [ "$USERS_FILE_CREATED" ] ; then
    echo ""
    echo ""
    echo "users.properties and groups.properties was created in the $kylo_home_folder folder with the dladmin user. For more information on configuring users and groups please see the \"Configure Access Control\" page in the Kylo Docs"
fi
echo ""
echo "If this is the first time installing Kylo, or you made hive/database related changes, you will need to modify those settings in the $kylo_home_folder/kylo-services/conf/application.properties file ";