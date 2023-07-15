#! /bin/bash

# Usage: <replicaId>

replicaId=$1

blockmess_port=`expr 11000 + $replicaId`
blockmess_wrapper_port=`expr 12000 + $replicaId`

network_name=blockmess_wrapper

replica_name=replica-$replicaId

interface=eth0
address=$replica_name
contact=replica-0:10000

docker network create $network_name

docker run --rm -d --name $replica_name -h $replica_name  \
	--network $network_name \
	-v "$(pwd)/blockmess/config:/app/config" \
	-v "$(pwd)/blockmess/keys:/app/keys" \
	-v "$(pwd)/blockmess/logs:/app/blockmess-logs" \
	henriquej0904/blockmess-wrapper-server \
	pt.unl.fct.di.blockmess.wrapper.server.tcp.BlockmessWrapperServerTCP \
	$blockmess_wrapper_port interface=$interface address=$address port=$blockmess_port contact=$contact

