#!/bin/bash
/usr/sbin/groupadd -f -r thinkbig 2> /dev/null || :
/usr/sbin/useradd -r -m -c "thinkbig user" -g thinkbig thinkbig 2> /dev/null || :