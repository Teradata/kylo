#!/bin/bash
echo "Welcome to the Think Big Data Lake Accelerator setup wizard. Lets get started !!!"
echo " "
echo "Please enter Y/y or N/n to the following questions:"
echo " "

while true
do
  # (1) prompt user, and read command line argument
  read -p "Would you like to install the database scripts to a local database instance? Please enter y/n: " install_db

  # (2) handle the input we were given
  case $install_db in
   [yY]* ) break;;

   [nN]* ) break;;

   * )     echo "Please enter Y or N";;
  esac
done

if [ "$install_db" == "y"  ] || [ "$install_db" == "Y" ] ; then
    echo "Which database (Enter the number)?"
    echo "1) MySQL"
    echo "2) Postgress"
    read database_type;
    echo "Please enter the database admin username";
    read username;

    echo "Please enter the database admin password";
    read -s password;
fi

echo "db type " $database_type;
echo " ";
read -p "Would you like me to install a local elasticsearch instance? Please enter y/n: " install_es

echo " ";
read -p "Would you like me to install a local activemq instance?  Please enter y/n: " install_activemq

echo " ";
read -p "Would you like me to install a local nifi instance? Please enter y/n: " install_nifi

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
    ./elasticsearch/install-elasticsearch.sh
fi

if [ "$install_activemq" == "y"  ] || [ "$install_activemq" == "Y" ] ; then
    ./activemq/install-activemq.sh
fi

if [ "$install_nifi" == "y"  ] || [ "$install_nifi" == "Y" ] ; then
    ./nifi/install-nifi.sh
    ./nifi/install-thinkbig-components.sh
fi