#! /bin/bash

# Usage: hostname port

network_name=blockmess_wrapper

docker run --rm -it  \
	--network $network_name \
	henriquej0904/blockmess-wrapper-client \
	'pt.unl.fct.di.blockmess.wrapper.client.tcp.BlockmessWrapperClientTCP$TestClientWrapper' \
	$@

