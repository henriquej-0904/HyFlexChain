#! /bin/bash

# Usage: hostname port

network_name=blockmess_wrapper

docker run --rm -it  \
	--network $network_name \
	-v "$(pwd)/blockmess/config:/app/config" \
	-v "$(pwd)/blockmess/keys:/app/keys" \
	-v "$(pwd)/blockmess/logs:/app/blockmess-logs" \
	henriquej0904/blockmess-wrapper \
	'pt.unl.fct.di.blockmess.wrapper.client.tcp.BlockmessWrapperClientTCP$TestClientWrapper' \
	$@

