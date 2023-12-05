#! /bin/bash

HOSTS=$(oarprint host | sort)

for HOST in $HOSTS;
do
    oarsh $HOST "sudo /etc/init.d/docker restart"
done

