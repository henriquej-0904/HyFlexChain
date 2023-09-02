#! /bin/bash

# Usage: <replicaId>

# Create a blockmess server

replicaId=$1

blockmess_port=`expr 12000 + $replicaId`
blockmess_port_2=`expr 1000 + $blockmess_port`
blockmess_wrapper_port=`expr 15000 + $replicaId`

network_name=hyflexchain

replica_name=blockmess-server-bft-smart-$replicaId

interface=eth0
address=$replica_name
contact=blockmess-server-bft-smart-0:12000
# address=$2
# contact=$3:12000

docker run --rm -d --name $replica_name -h $replica_name  \
	--network $network_name \
	--cap-add NET_ADMIN \
	-v "$(pwd)/hyflexchain-config:/app/hyflexchain-config" \
	-v "$(pwd)/blockmess/config:/app/blockmess-config" \
	-v "$(pwd)/blockmess/keys:/app/keys" \
	-v "$(pwd)/blockmess/logs:/app/blockmess-logs" \
	henriquej0904/hyflexchain:blockmess-wrapper-server-tc \
	hyflexchain-config/init_tc.sh \
	java -cp app.jar -Xmx2g pt.unl.fct.di.blockmess.wrapper.server.tcp.BlockmessWrapperServerTCP \
	$blockmess_wrapper_port interface=$interface address=$address port=$blockmess_port contact=$contact
