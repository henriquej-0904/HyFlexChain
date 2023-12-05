#! /bin/bash

mkdir -p config
cp -r /app/blockmess-config/* /app/config
cp -r hyflexchain-config/bft_smart/config/* /app/config

# setup tc to simulate network bandwidth and latency
tc qdisc add dev eth0 root netem rate 110mbit delay 75ms 5ms

eval $@
