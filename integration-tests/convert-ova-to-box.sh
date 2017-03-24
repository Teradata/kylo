#!/usr/bin/env bash

BASE_IMAGE=${BASE_IMAGE_NAME}-${BASE_IMAGE_VERSION}.ova

echo "Checking if $BASE_IMAGE already exists $DOWNLOAD_DIR"
if [ ! -e "$DOWNLOAD_DIR/$BASE_IMAGE" ]; then
    echo "Downloading sandbox from $BASE_IMAGE_URL/$BASE_IMAGE"
    echo "Do you already have sandbox handy? Place it into $DOWNLOAD_DIR/$BASE_IMAGE to re-use"
    curl -Lo "$DOWNLOAD_DIR/$BASE_IMAGE" "$BASE_IMAGE_URL/$BASE_IMAGE"
fi
echo "4. Download sandbox: OK"

if [ ! -e "$DOWNLOAD_DIR/Sandbox.box" ]; then
    echo "Converting .ova to .box"
    cp packer-template.json $HOME/.dls/packer-template.json
    sed -i -e "s@sandbox-path-replaced-by-script@$HOME/.dls/Sandbox.ova@g" $HOME/.dls/packer-template.json
    packer build $HOME/.dls/packer-template.json
    # all artifacts should live in $HOME/.dls directory, but found no way to
    # configure packer for that. maybe could use "cd $HOME/.dls" before running packer
    mv packer_*_virtualbox.box $HOME/.dls/Sandbox.box
    # remove packer cache because found it may cause problems for consecutive runs
    rm -R packer_cache
else
    echo "Skipping .ova convertion to .box because $HOME/.dls/Sandbox.box already exists"
fi
echo "5. Convert sandbox .ova to .box: OK"

