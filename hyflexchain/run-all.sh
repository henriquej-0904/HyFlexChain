#! /bin/bash

# run multiple servers in the same machine
# usage: <min_id> <max_id>

min_id=$1
max_id=$2


echo "Starting Blockmess Servers"

./run-blockmess_server.sh $min_id

sleep 3

for (( replicaId=$min_id+1; replicaId <= $max_id; replicaId++ ));
do
    ./run-blockmess_server.sh $replicaId
done

echo "Done"
sleep 5

echo "Starting HyFlexChain servers"

./run-server.sh $min_id

sleep 3

for (( replicaId=$min_id+1; replicaId <= $max_id; replicaId++ ));
do
    ./run-server.sh $replicaId
done

echo "Done"

