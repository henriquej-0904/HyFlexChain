#! /bin/bash

# Usage: <replicaId>

replicaId=$1

blockmess_port=`expr 10000 + $replicaId`
server_port=`expr 10800 + $replicaId`

network_name=blockmess

replica_name=replica-$replicaId

interface=eth0
address=$replica_name
contact=replica-0:10000

docker network create $network_name

docker run --rm -d --name $replica_name -h $replica_name  \
	--network blockmess -p $server_port:$server_port \
	-v "$(pwd)/tls-config/$replica_name:/app/tls-config/$replica_name" \
	-v "$(pwd)/tls-config/truststore.pkcs12:/app/tls-config/truststore.pkcs12" \
	-v "$(pwd)/blockmess/config:/app/config" \
	-v "$(pwd)/blockmess/keys:/app/keys" \
	-v "$(pwd)/blockmess/logs:/app/blockmess-logs" \
	henriquej0904/blockmess-simple-crypto-node \
	$replicaId $server_port $blockmess_port interface=$interface address=$address contact=$contact

