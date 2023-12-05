#! /bin/bash

for HOST in $(oarprint host); 
do
 	oarsh $HOST "docker swarm leave -f";
done

