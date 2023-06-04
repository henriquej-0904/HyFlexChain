#! /bin/bash

docker run --rm -it --workdir="/project" -v "$(pwd):/project" \
    -v "/etc/passwd:/etc/passwd:ro" -u $UID web3labs/web3j $@