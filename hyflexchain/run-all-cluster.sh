#! /bin/bash

# run multiple servers in the same machine
# usage: <min_id> <max_id> <address> <contact>

min_id=$1
max_id=$2


# echo "Starting Blockmess Servers"

# ./run-blockmess_server.sh $min_id $3 $4

# sleep 3

# for (( replicaId=$min_id+1; replicaId <= $max_id; replicaId++ ));
# do
#     ./run-blockmess_server.sh $replicaId $3 $4
# done

# echo "Done"
# sleep 5

echo "Starting HyFlexChain servers"

./run-server-cluster.sh $min_id $3 $4

sleep 3

for (( replicaId=$min_id+1; replicaId <= $max_id; replicaId++ ));
do
    ./run-server-cluster.sh $replicaId $3 $4
done

echo "Done"

