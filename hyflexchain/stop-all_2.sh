#! /bin/bash

# stop multiple servers in the same machine
# usage: <min_id> <max_id>

min_id=$1
max_id=$2

for (( replicaId=$min_id; replicaId <= $max_id; replicaId++ ));
do
    docker stop blockmess-server-bft-smart-$replicaId
    docker stop replica-$replicaId
done
