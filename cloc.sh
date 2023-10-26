#! /bin/bash

docker run --rm -v $PWD:/tmp --workdir "/tmp" aldanial/cloc blockmess-wrapper/ caliper/caliper-hyflexchain/lib/ hyflexchain/ smart-contracts-besu-evm/

