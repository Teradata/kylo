#!/bin/bash

if [ -z $1 ] || [ -z $2 ]; then
    echo "Usage: release.sh <release version> <next version>"
    exit 1
fi

RELEASE_VERSION=$1
NEXT_VERSION=$2

set -x

#############################################################
## Cleanup any existing branches and/or tags from a prior run
#############################################################
git tag -d v$RELEASE_VERSION  &>/dev/null  
git branch -D release-$RELEASE_VERSION  &>/dev/null  
git branch -D point-$RELEASE_VERSION  &>/dev/null  

################
## Master branch
################

git checkout master
git pull
git checkout -b release-$RELEASE_VERSION

mvn versions:set versions:update-child-modules -DgenerateBackupPoms=false  -DnewVersion=$RELEASE_VERSION

git commit -a -m "Release $RELEASE_VERSION"
git tag v$RELEASE_VERSION

mvn versions:set versions:update-child-modules -DgenerateBackupPoms=false  -DnewVersion=$NEXT_VERSION-SNAPSHOT

git commit -a -m "Begin $NEXT_VERSION-SNAPSHOT"
git checkout master
git merge release-$RELEASE_VERSION

git push --tags
##git branch -d release-$RELEASE_VERSION


#######################
## Point release branch
#######################

git checkout -b point-$RELEASE_VERSION v$RELEASE_VERSION

mvn versions:set versions:update-child-modules -DgenerateBackupPoms=false  -DnewVersion=$RELEASE_VERSION.1-SNAPSHOT

git commit -a -m "Begin $RELEASE_VERSION.1-SNAPSHOT"
git push point-$RELEASE_VERSION:release/$RELEASE_VERSION
##git branch -d point-$RELEASE_VERSION

exit 0
