#! /bin/bash

docker swarm init --advertise-addr bond0
JOIN_TOKEN=$(docker swarm join-token manager -q)
MASTER=$(hostname)
echo "Initialized docker swarm on leader $MASTER"
for HOST in $(oarprint host); 
do
	if [ $HOST != $MASTER ]; 
	then
		echo "Joining swarm with host $HOST"
 		oarsh $HOST "docker swarm join --token $JOIN_TOKEN $MASTER:2377"
	fi
done

