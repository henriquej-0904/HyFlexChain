#! /bin/bash

# Usage: <replicaId>

replicaId=$1

blockmess_port=`expr 10000 + $replicaId`
server_port=`expr 10800 + $replicaId`

docker run --name "replica-$replicaId" --rm -d --network host \
-v "$(pwd)/tls-config/replica-$replicaId:/app/tls-config/replica-$replicaId" \
-v "$(pwd)/tls-config/truststore.pkcs12:/app/tls-config/truststore.pkcs12" \
-v "$(pwd)/blockmess/config:/app/config" \
-v "$(pwd)/blockmess/keys:/app/keys" \
-v "$(pwd)/blockmess/logs:/app/blockmess-logs" \
henriquej0904/blockmess-simple-crypto-node $replicaId $server_port $blockmess_port