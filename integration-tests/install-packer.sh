#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )



if [ ! -e "$DOWNLOAD_DIR/packer.zip" ]; then
    echo "Downloading Packer to $DOWNLOAD_DIR ..."
    curl -Lo "$DOWNLOAD_DIR/packer.zip" "$PACKER_URL"
fi
echo "Download Packer: OK"

command -v packer >/dev/null 2>&1 || {
    echo "Packer it's not installed.  Installing Packer...";
    if [ ! -e "$HOME/packer" ]; then
        echo "Unzipping Packer to $HOME/packer"
        unzip -d $HOME/packer ${DOWNLOAD_DIR}/packer.zip
    fi
    echo "Installing Packer by adding $HOME/packer to PATH"
    export PATH=$PATH:$HOME/packer
    echo "PATH=$PATH"
}

command -v packer >/dev/null 2>&1 || {
    echo "Failed to install Packer, aborting";
    exit 1;
}
echo "Install Packer: OK"
