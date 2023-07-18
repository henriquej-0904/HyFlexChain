#! /bin/bash

docker build -t henriquej0904/caliper-hyflexchain .

docker run --rm -it --network host \
    -v "$(pwd)/config:/hyperledger/caliper/workspace/config" \
    -v "$(pwd)/crypto:/hyperledger/caliper/workspace/crypto" \
    -v /var/run/docker.sock:/var/run/docker.sock \
    --name caliper henriquej0904/caliper-hyflexchain  # caliper launch manager --caliper-bind-file sut.yaml
