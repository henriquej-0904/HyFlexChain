#! /bin/bash

HOSTS=$(oarprint host | sort)

for HOST in $HOSTS;
do
    echo -e "\nDocker pull to $HOST"
    oarsh $HOST docker pull henriquej0904/hyflexchain:hyflexchain-tc
    oarsh $HOST docker pull henriquej0904/hyflexchain:caliper
    oarsh $HOST docker pull henriquej0904/hyflexchain:blockmess-wrapper-server-tc;
done

echo Done
