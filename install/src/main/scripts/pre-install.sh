#!/bin/bash
echo "Installing data lake accelerator"
echo "Creating thinkbig user"
if [ $(getent passwd thinkbig) ] ; then
    echo "The thinkbig user already exists"
else
    useradd -m thinkbig -d /opt/thinkbig
fi