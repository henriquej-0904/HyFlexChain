#! /bin/bash

# Usage: <replicaId>

replicaId=$1

blockmess_port=`expr 10000 + $replicaId`
server_port=`expr 10800 + $replicaId`

network_name=hyflexchain

replica_name=replica-$replicaId

address=$replica_name
contact=replica-0:10000

docker network create $network_name

docker run --rm -d --name $replica_name -h $replica_name  \
	--network $network_name -p $server_port:$server_port \
	-v "$(pwd)/tls-config/$replica_name:/app/tls-config/$replica_name" \
	-v "$(pwd)/tls-config/truststore.pkcs12:/app/tls-config/truststore.pkcs12" \
	-v "$(pwd)/blockmess/config:/app/config" \
	-v "$(pwd)/blockmess/keys:/app/keys" \
	-v "$(pwd)/blockmess/logs:/app/blockmess-logs" \
	-v "$(pwd)/config:/app/hyflexchain-config" \
	henriquej0904/hyflexchain \
	java -cp hyflexchain.jar pt.unl.fct.di.hyflexchain.api.rest.impl.server.HyFlexChainServer \
	$replicaId $server_port /app/hyflexchain-config \
	-POW BLOCKMESS_PORT=$blockmess_port \
	-POW BLOCKMESS_ADDRESS=$address \
	-POW BLOCKMESS_CONTACT=$contact


