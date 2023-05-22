#! /bin/bash

docker build -t caliper-hyflexchain .

docker run --rm -it --network host \
    -v "$(pwd)/config:/hyperledger/caliper/workspace/config" \
    -v /var/run/docker.sock:/var/run/docker.sock \
    --name caliper caliper-hyflexchain  # caliper launch manager --caliper-bind-file sut.yaml
