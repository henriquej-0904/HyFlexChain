#! /bin/bash

id=$1
results_folder=$2

mkdir -p $results_folder

docker run --rm -d --network host \
    -v "$(pwd)/config:/hyperledger/caliper/workspace/config" \
    -v "$(pwd)/crypto:/hyperledger/caliper/workspace/crypto" \
    -v "$(pwd)/$results_folder:/hyperledger/caliper/workspace/results" \
    -v /var/run/docker.sock:/var/run/docker.sock \
    --name caliper henriquej0904/hyflexchain:caliper /bin/sh -c "./launch-master.sh && cp report.html results/report-$id"

docker wait caliper
