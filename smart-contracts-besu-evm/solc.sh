#! /bin/bash

# CONTRACT=$1
# OUTPUT=$2

# Example
# /solc.sh -o smart-contracts/output --abi --bin --asm --gas --overwrite --evm-version london smart-contracts/Storage.sol

docker run --rm -v "$(pwd)/smart-contracts:/smart-contracts" \
    -v "/etc/passwd:/etc/passwd:ro" -u $UID ethereum/solc:stable \
    --abi --bin --asm --gas --overwrite --evm-version london \
    $@
