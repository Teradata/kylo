#!/bin/bash
rpmInstallDir=/opt/thinkbig

echo "   REMOVING thinkbig-data-lake-accelerator ... "
##copy any conf and plugins before installing
{
time_stamp=$(date +%Y_%m_%d_%H_%M_%s)
mkdir -p $rpmInstallDir/bkup-config/$time_stamp
bkupDir=$rpmInstallDir/bkup-config/$time_stamp
###find the rpm that is installed
lastRpm=$(rpm -q --last thinkbig-data-lake-accelerator)
touch ${bkupDir}/README.txt
readme=${bkupDir}/README.txt

echo "    1. Backup Configuration "
echo "        - Backing up previous configuration files "
echo "        - Copying previous /conf folder"
echo "        - Contents in this directory is for thinkbig-data-lake-accelerator RPM: $lastRpm " > $readme
cp -r $rpmInstallDir/thinkbig-ui/conf $bkupDir/thinkbig-ui
cp -r $rpmInstallDir/thinkbig-services/conf $bkupDir/thinkbig-services
echo "        - BACKUP COMPLETE!! "
echo "        - Backup Configuration is located at : $bkupDir "
echo "        - A README.txt file will be included in the backup directory indicating what RPM these files came from "
}