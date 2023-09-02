#! /bin/bash

# kill running containers in a cluster


NUM_NODES=20
CONTACT_HOST=$(oarprint host | sort | head -n 1)
HOSTS=$(oarprint host | sort | head -n 4)
NUM_HOSTS=$(oarprint host | sort | head -n 4 | wc -l)
NODES_PER_HOST=$(($NUM_NODES / $NUM_HOSTS))

echo "Stopping..."

NODE_IDX=0

for ((i=0;i<$NODES_PER_HOST;i++));
do
    for HOST in $HOSTS;
    do
        oarsh $HOST "cd HyFlexChain/hyflexchain && docker kill blockmess-server-bft-smart-$NODE_IDX"
        oarsh $HOST "cd HyFlexChain/hyflexchain && docker kill replica-$NODE_IDX"

        NODE_IDX=$(($NODE_IDX + 1));
    done;
done

echo "Done"
sleep 5