#!/bin/bash

offline=false
if [ $# -gt 1 ]
then
    if [ "$1" = "-o" ] || [ "$1" = "-O" ]
    then
        echo "Working in offline mode"
        offline=true
    fi
fi

current_dir=$(pwd)
echo "The working directory is $current_dir"

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

    # Only supporting MySql at this time
    database_type=0
    while [[ ! ${database_type} =~ ^[1-2]{1}$ ]]; do
        echo "Which database (Enter the number)?"
        echo "1) MySQL"
        echo "2) PostgresSQL"
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
            ! mysql -h ${hostname} -u ${username} --password=${password} -e "create database if not exists kylo character set utf8 collate utf8_general_ci;" || break;
        fi
        if [ "$database_type" == "2"  ] ; then
            echo "Creating PostgreSQL database 'kylo'"
            ! PGPASSWORD=${password} createdb -U kylo -h ${hostname} -E UTF8 -e kylo || break;
        fi
    done
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
    fi

    if [[ -z "$activemq_home" ]]; then
        activemq_home=/opt/activemq
    fi
done

echo " ";
while [[ ! $install_nifi =~ $yes_no ]]; do
    read -p "Would you like me to install a local nifi instance? Please enter y/n: " install_nifi

    if [ "$install_nifi" == "y"  ] || [ "$install_nifi" == "Y" ] ; then
         read -p "Enter the NiFi home folder location, hit Enter for '/opt/nifi': " nifi_home
    fi

    if [[ -z "$nifi_home" ]]; then
        nifi_home=/opt/nifi
    fi
done

while [[ ! $java_type =~ ^[1-4]{1}$ ]]; do
    echo "Please choose an option to configure Java"
    echo "1) I already have Java 8 or higher installed as the system Java and want to use that"
    echo "2) Install Java 8 in the /opt/java folder for me and use that one"
    echo "3) I have Java 8 or higher installed in another location already. I will provide the location"
    echo "4) Java is already setup. No changes necessary"
    read -p "> " java_type;
done

if [ "$java_type" == "3" ] ; then
    read -p "Please enter the location to the JAVA_HOME: " java_home
fi

if [ $offline = true ]
then
    cd $current_dir
else
    cd $kylo_home_folder/setup
fi

if [ "$java_type" == "1" ] ; then
    echo "Using system Java 8 and remove the JAVA_HOME variable from kylo-ui and kylo-services"
    ./java/remove-default-kylo-java-home.sh $kylo_home_folder
elif [ "$java_type" == "2" ] ; then
    if [ $offline = true ]
    then
        ./java/install-java8.sh $current_dir -O
    else
        ./java/install-java8.sh $kylo_home_folder
    fi

elif [ "$java_type" == "3" ] ; then
    ./java/remove-default-kylo-java-home.sh $kylo_home_folder
    ./java/change-kylo-java-home.sh $java_home $kylo_home_folder
fi

if [ "$install_es" == "y"  ] || [ "$install_es" == "Y" ] ; then
    echo "Installing Elasticsearch"
    if [ $offline = true ]
    then
        ./elasticsearch/install-elasticsearch.sh $current_dir -O
    else
        ./elasticsearch/install-elasticsearch.sh
    fi
fi

if [ "$install_activemq" == "y"  ] || [ "$install_activemq" == "Y" ] ; then
    echo "installing ActiveMQ"
    if [ $offline = true ]
    then
        ./activemq/install-activemq.sh $activemq_home activemq activemq $current_dir -O
    else
        ./activemq/install-activemq.sh $activemq_home activemq activemq
    fi

fi

if [ "$install_nifi" == "y"  ] || [ "$install_nifi" == "Y" ] ; then
    if [ $offline = true ]
    then
        ./nifi/install-nifi.sh $nifi_home nifi users $current_dir -O
    else
        ./nifi/install-nifi.sh $nifi_home nifi users
    fi


    if [ "$java_type" == "2" ] ; then
        ./java/change-nifi-java-home.sh /opt/java/current $nifi_home/current
    elif  [ "$java_type" == "3" ] ; then
        ./java/change-nifi-java-home.sh $java_home $nifi_home/current
    fi

    if [ $offline = true ]
    then
        ./nifi/install-kylo-components.sh $nifi_home $kylo_home_folder nifi users $current_dir -O
    else
        ./nifi/install-kylo-components.sh $nifi_home $kylo_home_folder nifi users
    fi

    sed -i "s|kylo.provenance.cache.location=\/opt\/nifi\/feed-event-statistics.gz|kylo.provenance.cache.location=$nifi_home\/feed-event-statistics.gz|" $nifi_home/ext-config/config.properties


fi
