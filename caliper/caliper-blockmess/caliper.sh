#! /bin/bash

docker run --rm -it --network host \
    -v "$(pwd):/hyperledger/caliper/workspace" \
    -e CALIPER_BIND_SUT=blockmess:1.0 \
    -e CALIPER_BENCHCONFIG=benchmark.yaml \
    -e CALIPER_NETWORKCONFIG=networkconfig.json \
    --name caliper hyperledger/caliper:0.5.0 launch manager --caliper-bind-file sut.yaml