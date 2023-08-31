#! /bin/bash

# Usage: <replicaId> <address> <contact>

replicaId=$1

blockmess_port=`expr 10000 + $replicaId`
blockmess_port_2=`expr 1000 + $blockmess_port`
server_port=`expr 18000 + $replicaId`

network_name=hyflexchain

replica_name=replica-$replicaId

# address=$replica_name
# contact=replica-0:10000
address=$2
contact=$3

# docker network create $network_name

docker run --rm -d --name $replica_name -h $replica_name  \
	--network $network_name -p $server_port:$server_port -p $blockmess_port:$blockmess_port \
	-p $blockmess_port_2:$blockmess_port_2 \
	--cap-add NET_ADMIN \
	-v "$(pwd)/hyflexchain-config:/app/hyflexchain-config" \
	-v "$(pwd)/blockmess/config:/app/blockmess-config" \
	-v "$(pwd)/blockmess/keys:/app/keys" \
	-v "$(pwd)/blockmess/logs:/app/blockmess-logs" \
	henriquej0904/hyflexchain-tc \
	hyflexchain-config/init_tc.sh java -Xmx2g -cp hyflexchain.jar:lib-bft-smart/* pt.unl.fct.di.hyflexchain.api.rest.impl.server.HyFlexChainServer \
	$replicaId $server_port /app/hyflexchain-config \
	-G KEYSTORE=hyflexchain-config/keys/$replica_name/keystore.pkcs12 \
	-G KEYSTORE_ALIAS=$replica_name \
	-POW BLOCKMESS_PORT=$blockmess_port \
	-POW BLOCKMESS_ADDRESS=$address \
	-POW BLOCKMESS_CONTACT=$contact \
	-BFT_SMART BFT_SMaRt_Replica_Id=$replicaId \
	-BFT_SMART Blockmess_Connector_Host=localhost \
	-BFT_SMART Blockmess_Connector_Port=`expr 15000 + $replicaId`
	


