#!/bin/bash

# remove nifi
service nifi stop
rm -rf /opt/nifi
rm -rf /var/log/nifi
rm -f /etc/init.d/nifi
userdel nifi