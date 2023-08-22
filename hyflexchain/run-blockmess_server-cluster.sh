#! /bin/bash

# Usage: <replicaId> <address> <contact>

# Create a blockmess server

replicaId=$1

blockmess_port=`expr 12000 + $replicaId`
blockmess_wrapper_port=`expr 15000 + $replicaId`

network_name=host

replica_name=blockmess-server-bft-smart-$replicaId

interface=eth0
# address=$replica_name
# contact=blockmess-server-bft-smart-0:12000
address=$2
contact=$3

# docker network create $network_name

docker run --rm -d --name $replica_name -h $replica_name  \
	--network $network_name \
	-v "$(pwd)/blockmess/config:/app/config" \
	-v "$(pwd)/blockmess/keys:/app/keys" \
	-v "$(pwd)/blockmess/logs:/app/blockmess-logs" \
	henriquej0904/blockmess-wrapper-server \
	-Xmx1g pt.unl.fct.di.blockmess.wrapper.server.tcp.BlockmessWrapperServerTCP \
	$blockmess_wrapper_port interface=$interface address=$address port=$blockmess_port contact=$contact

