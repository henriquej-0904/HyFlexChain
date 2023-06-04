#! /bin/bash

docker run --rm -it -v "$(pwd):/project" \
    -v "/etc/passwd:/etc/passwd:ro" -u $UID web3labs/web3j $@