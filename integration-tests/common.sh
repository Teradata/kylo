#!/usr/bin/env bash

if [ ! -e "$DOWNLOAD_DIR" ]; then
    mkdir ${DOWNLOAD_DIR}
fi

case "$OSTYPE" in
  solaris*) echo "SOLARIS is not supported"; exit 1 ;;
  darwin*)  echo "Running OSX"; PACKER_URL=${PACKER_URL_OSX} ;;
  linux*)   echo "Running LINUX"; PACKER_URL=${PACKER_URL_LINUX} ;;
  bsd*)     echo "BSD is not supported"; exit 1 ;;
  msys*)    echo "WINDOWS is not supported"; exit 1 ;;
  *)        echo "OS Type: $OSTYPE is not supported"; exit 1 ;;
esac


