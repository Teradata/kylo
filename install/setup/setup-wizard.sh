#!/bin/bash


validate-system() {
	if [ -z ${SPARK_PROFILE} ] && ! which spark-submit;  then 
		>&2 echo "Spark not installed or version not found.  Install spark and try again";
		exit 1
	fi	
	if ! id kylo >/dev/null 2>&1 ; then
		>&2 echo "user 'kylo' not found.  Create user and try again"
		exit 1
	fi
	if ! id nifi >/dev/null 2>&1; then
		>&2 echo "user 'nifi' not found.  Create user and try again"
		exit 1
	fi
	if ! id activemq >/dev/null 2>&1; then
		>&2 echo "user 'activemq' not found.  Create user and try again"
		exit 1
	fi
}

validate-system

offline=false
if [ $# > 1 ]
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
while [[ ! $install_db =~ $yes_no ]]; do
    read -p "Would you like to install the database scripts to a local database instance? Please enter y/n: " install_db
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
done

echo " ";
while [[ ! $install_nifi =~ $yes_no ]]; do
    read -p "Would you like me to install a local nifi instance? Please enter y/n: " install_nifi
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
    cd /opt/kylo/setup
fi

#if [ "$install_db" == "y"  ] || [ "$install_db" == "Y" ] ; then
#    echo "Running the database scripts"
#
#    if [ "$database_type" == "1"  ] ; then
#        echo "Installing for MySQL"
#        ./sql/mysql/setup-mysql.sh $hostname $username $password
#    fi
#    if [ "$database_type" == "2"  ] ; then
#        echo "Installation for Postgres is not yet supported"
#    fi
#
#fi

if [ "$install_es" == "y"  ] || [ "$install_es" == "Y" ] ; then
    echo "Installing Elasticsearch"
    if [ $offline = true ]
    then
        ./elasticsearch/install-elasticsearch.sh -O $current_dir
    else
        ./elasticsearch/install-elasticsearch.sh
    fi
fi

if [ "$install_activemq" == "y"  ] || [ "$install_activemq" == "Y" ] ; then
    echo "installing ActiveMQ"
    if [ $offline = true ]
    then
        ./activemq/install-activemq.sh -O $current_dir
    else
        ./activemq/install-activemq.sh
    fi

fi

if [ "$java_type" == "1" ] ; then
    echo "Using system Java 8 and remove the JAVA_HOME variable from kylo-ui and kylo-services"
    ./java/remove-default-kylo-java-home.sh
elif [ "$java_type" == "2" ] ; then
    if [ $offline = true ]
    then
        ./java/install-java8.sh -O $current_dir
    else
        ./java/install-java8.sh
    fi

elif [ "$java_type" == "3" ] ; then
    ./java/remove-default-kylo-java-home.sh
    ./java/change-kylo-java-home.sh $java_home
fi

if [ "$install_nifi" == "y"  ] || [ "$install_nifi" == "Y" ] ; then
    if [ $offline = true ]
    then
        ./nifi/install-nifi.sh -O $current_dir
    else
        ./nifi/install-nifi.sh
    fi

    if [ "$java_type" == "2" ] ; then
        ./java/change-nifi-java-home.sh /opt/java/current
    elif  [ "$java_type" == "3" ] ; then
        ./java/change-nifi-java-home.sh $java_home
    fi

    if [ $offline = true ]
    then
        ./nifi/install-kylo-components.sh -O $current_dir
    else
        ./nifi/install-kylo-components.sh
    fi

fi
