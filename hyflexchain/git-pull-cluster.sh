#! /bin/bash

HOSTS=$(oarprint host | sort)

for HOST in $HOSTS;
do
    echo -e "\nGit pull to $HOST"
    oarsh $HOST cd HyFlexChain && git pull;
done

echo Done