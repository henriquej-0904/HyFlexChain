#! /bin/bash

# Generate configuration for the specified number of faults (and consequently number of replicas)

# Usage: <F>

if test -z "$1"; then
    echo "Usage: <F>"
fi

F=$1
N=$((3 * F))

mkdir -p configs
mkdir -p configs/"$F"f

HOSTS=configs/"$F"f/hosts.config

cp base-config/hosts.config $HOSTS

for i in $(seq 0 $N);
do
    echo "$i replica-$i 20000 20001" >> $HOSTS;
done

echo "system.servers.num = $((N + 1))"
echo "system.servers.f = $F"
echo "system.initial.view = $(seq -s, 0 $N)"