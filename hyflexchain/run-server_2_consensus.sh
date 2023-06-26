#! /bin/bash

# Usage: <replicaId>

replicaId=$1

blockmess_port=`expr 10000 + $replicaId`
server_port=`expr 18000 + $replicaId`

network_name=hyflexchain

replica_name=replica-$replicaId

address=$replica_name
contact=replica-0:10000

docker network create $network_name

docker run --rm -d --name $replica_name -h $replica_name  \
	--network $network_name -p $server_port:$server_port \
	-v "$(pwd)/tls-config/$replica_name:/app/tls-config/$replica_name" \
	-v "$(pwd)/tls-config/truststore.pkcs12:/app/tls-config/truststore.pkcs12" \
	-v "$(pwd)/hyflexchain-config:/app/hyflexchain-config" \
	-v "$(pwd)/blockmess/config:/app/blockmess-config" \
	-v "$(pwd)/blockmess/keys:/app/keys" \
	-v "$(pwd)/blockmess/logs:/app/blockmess-logs" \
	henriquej0904/hyflexchain \
	hyflexchain-config/init.sh java -cp hyflexchain.jar:lib-bft-smart/* pt.unl.fct.di.hyflexchain.api.rest.impl.server.HyFlexChainServer \
	$replicaId $server_port /app/hyflexchain-config \
	-POW BLOCKMESS_PORT=$blockmess_port \
	-POW BLOCKMESS_ADDRESS=$address \
	-POW BLOCKMESS_CONTACT=$contact \
	-BFT_SMART BFT_SMaRt_Replica_Id=$replicaId \
	-BFT_SMART Blockmess_Connector_Host=blockmess-server-bft-smart-$replicaId \
	-BFT_SMART Blockmess_Connector_Port=`expr 11000 + $replicaId`
	


