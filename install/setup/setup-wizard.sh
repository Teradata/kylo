#!/bin/bash
echo "Welcome to the Think Big Data Lake Accelerator setup wizard. Lets get started !!!"
echo " "
echo "Please enter Y/y or N/n to the following questions:"
echo " "

yes_no="^[yYnN]{1}$"
# (1) prompt user, and read command line argument
while [[ ! $install_db =~ $yes_no ]]; do
    read -p "Would you like to install the database scripts to a local database instance? Please enter y/n: " install_db
done

if [ "$install_db" == "y"  ] || [ "$install_db" == "Y" ] ; then
    while [[ ! $database_type =~ ^[1]{1}$ ]]; do
        echo "Which database (Enter the number)?"
        echo "1) MySQL"
        echo "2) Postgress (Not yet supported)"
        read -p "> " database_type;
    done
    echo "Please enter the database ADMIN username";
    read -p "> " username;

    while ! mysql -u root -p$password  -e ";" ; do
       echo "Please enter the database ADMIN password";
       read -p "> " -s password;
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

while [[ ! $java_type =~ ^[1-3]{1}$ ]]; do
    echo "Please choose an option to configure Java"
    echo "1) I already have Java 8 or higher installed as the system Java and want to use that"
    echo "2) Download and install Java 8 in the /opt/java folder for me and use that one"
    echo "3) I have Java 8 or higher installed in another location already. I will provide the location"
    read -p "> " java_type;
done

if [ "$java_type" == "3" ] ; then
    read -p "Please enter the location to the JAVA_HOME: " java_home
fi

cd /opt/thinkbig/setup

if [ "$install_db" == "y"  ] || [ "$install_db" == "Y" ] ; then
    echo "Running the database scripts"

    if [ "$database_type" == "1"  ] ; then
        echo "Installing for MySQL"
        ./sql/mysql/setup-mysql.sh $username $password
    fi
    if [ "$database_type" == "2"  ] ; then
        echo "Installation for Postgres is not yet supported"
    fi

fi

if [ "$install_es" == "y"  ] || [ "$install_es" == "Y" ] ; then
    echo "Installing Elasticsearch"
    ./elasticsearch/install-elasticsearch.sh
fi

if [ "$install_activemq" == "y"  ] || [ "$install_activemq" == "Y" ] ; then
    echo "installing ActiveMQ"
    ./activemq/install-activemq.sh
fi

if [ "$java_type" == "1" ] ; then
    echo "Using system Java 8 and remove the JAVA_HOME variable from thinkbig-ui and thinkbig-services"
    ./java/remove-default-thinkbig-java-home.sh
elif [ "$java_type" == "2" ] ; then
    ./java/install-java8.sh

elif [ "$java_type" == "3" ] ; then
    ./java/remove-default-thinkbig-java-home.sh
    ./java/change-thinkbig-java-home.sh $java_home
fi

if [ "$install_nifi" == "y"  ] || [ "$install_nifi" == "Y" ] ; then
    ./nifi/install-nifi.sh
    if [ "$java_type" == "2" ] ; then
        ./java/change-nifi-java-home.sh /opt/java/current
    elif  [ "$java_type" == "3" ] ; then
        ./java/change-nifi-java-home.sh $java_home
    fi
    ./nifi/install-thinkbig-components.sh
fi