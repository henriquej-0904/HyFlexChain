#! /bin/bash

# docker build -t henriquej0904/hyflexchain:caliper .

docker run --rm -it --network host \
    -v "$(pwd)/config:/hyperledger/caliper/workspace/config" \
    -v "$(pwd)/crypto:/hyperledger/caliper/workspace/crypto" \
    -v /var/run/docker.sock:/var/run/docker.sock \
    --name caliper-$1 henriquej0904/hyflexchain:caliper  # caliper launch manager --caliper-bind-file sut.yaml
