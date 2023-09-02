#! /bin/bash

NUM_NODES=20
PORT=18000

docker run --rm -it --network hyflexchain henriquej0904/hyflexchain:hyflexchain-tc \
/bin/bash -c 'for i in $(seq 0 `expr $NUM_NODES - 1`); do echo "Reset replica-$i" && curl -k "https://replica-$i:`expr $PORT + $i`/api/rest/hyflexchain/private/settings/reset"; done'
