#! /bin/bash

# run multiple servers in the same machine
# usage: <min_id> <max_id>

min_id=$1
max_id=$2

# for (( replicaId=$min_id; replicaId <= $max_id; replicaId++ ));
# do
#     ./run-blockmess_server_bft_smart.sh $replicaId
# done

for (( replicaId=$min_id; replicaId <= $max_id; replicaId++ ));
do
    ./run-server_2_consensus.sh $replicaId
done

