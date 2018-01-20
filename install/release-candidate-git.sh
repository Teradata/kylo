#!/bin/bash

if [ -z $1 ] ; then
    echo "Usage: release-candidate-git.sh <release version> "
    echo "Example releasing 0.8.4-RC1 from master branch > release-candidate-git.sh 0.8.4-RC1 "
    exit 1
fi


RELEASE_VERSION=$1

set -x

setVersion() {
  mvn versions:set versions:update-child-modules -DgenerateBackupPoms=false  -DnewVersion=$1
  find ui -name package.json -a \! -path '*node_modules*' -exec sed -i '' "s/\"version\": \"[0-9.A-Z-]*\"/\"version\": \"$1\"/" {} \;
  sed -i '' "s/\"\?ver=[0-9.A-Z-]*\"/\"?ver=$1\"/" ui/ui-app/src/main/resources/static/js/systemjs.config.js
  mvn package -f services/upgrade-service
}

################
## Master branch
################

git checkout master
git pull
git checkout -b release-candidate-$RELEASE_VERSION

###mvn versions:set versions:update-child-modules -DgenerateBackupPoms=false -DnewVersion=$RELEASE_VERSION

setVersion ${RELEASE_VERSION}

git commit -a -m "Release $RELEASE_VERSION"

git tag v$RELEASE_VERSION
git push origin --tags

git checkout master
git branch -D release-candidate-$RELEASE_VERSION

exit 0