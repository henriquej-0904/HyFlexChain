#! /bin/bash

network_name=hyflexchain

docker network create $network_name

docker run --rm -it  \
	--network $network_name --entrypoint "" \
	-v "$(pwd)/tls-config/$replica_name:/app/tls-config/$replica_name" \
	-v "$(pwd)/tls-config/truststore.pkcs12:/app/tls-config/truststore.pkcs12" \
	-v "$(pwd)/blockmess/config:/app/config" \
	-v "$(pwd)/blockmess/keys:/app/keys" \
	-v "$(pwd)/blockmess/logs:/app/blockmess-logs" \
	-v "$(pwd)/hyflexchain-config:/app/hyflexchain-config" \
	henriquej0904/hyflexchain $@


