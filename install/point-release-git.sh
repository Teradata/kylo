#!/bin/bash

if [ -z $1 ] || [ -z $2 ] || [ -z $3 ]; then
    echo "Usage: point-release.sh <release branch version> <pont-release-number> <next version number> "
    echo "Example releasing 0.8.2.2 from release/0.8.2 branch > point-release-git.sh 0.8.2 2 3 "
    exit 1
fi


RELEASE_VERSION=$1
POINT_RELEASE_NUMBER=$2
NEXT_NUMBER=$3

set -x

setVersion() {
  mvn versions:set versions:update-child-modules -DgenerateBackupPoms=false  -DnewVersion=$1
  find ui -name package.json -a \! -path '*node_modules*' -exec sed -i '' "s/\"version\": \"[0-9.A-Z-]*\"/\"version\": \"$1\"/" {} \;
  sed -i '' "s/\"\?ver=[0-9.A-Z-]*\"/\"?ver=$1\"/" ui/ui-app/src/main/resources/static/js/systemjs.config.js
  mvn package -f services/upgrade-service
}

#######################
## Point release branch
#######################

git checkout release/$RELEASE_VERSION
git pull
git checkout -b point-$RELEASE_VERSION release/$RELEASE_VERSION


setVersion ${RELEASE_VERSION}.${POINT_RELEASE_NUMBER}

git commit -a -m "Release $RELEASE_VERSION.$POINT_RELEASE_NUMBER"

git tag v$RELEASE_VERSION.$POINT_RELEASE_NUMBER

setVersion ${RELEASE_VERSION}.${NEXT_NUMBER}-SNAPSHOT

git commit -a -m "Begin $RELEASE_VERSION.$NEXT_NUMBER-SNAPSHOT"

git checkout release/$RELEASE_VERSION
git merge point-$RELEASE_VERSION
git push origin head:release/$RELEASE_VERSION
git push origin --tags
git branch -d point-$RELEASE_VERSION

exit 0