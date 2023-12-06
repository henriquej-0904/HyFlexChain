#! /bin/bash

# docker run --rm -it --network host \
#     -v "$(pwd):/hyperledger/caliper/workspace" \
#     -e CALIPER_BIND_SUT=blockmess:1.0 \
#     -e CALIPER_BENCHCONFIG=benchmark.yaml \
#     -e CALIPER_NETWORKCONFIG=networkconfig.json \
#     --name caliper hyperledger/caliper:0.5.0 launch manager --caliper-bind-file sut.yaml

docker build -t caliper-blockmess .

docker run --rm -it --network host \
    -v "$(pwd)/config:/hyperledger/caliper/workspace/config" \
    -v /var/run/docker.sock:/var/run/docker.sock \
    --name caliper caliper-blockmess  # caliper launch manager --caliper-bind-file sut.yaml
