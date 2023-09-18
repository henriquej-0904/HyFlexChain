#! /bin/bash

# Get blockmess latency times


NUM_NODES=$1
CONTACT_HOST=$(oarprint host | sort | head -n 1)
HOSTS=$(oarprint host | sort | head -n 4)
NUM_HOSTS=$(oarprint host | sort | head -n 4 | wc -l)
NODES_PER_HOST=$(($NUM_NODES / $NUM_HOSTS))

echo "Get test blockmess latency results"

NODE_IDX=0

folder=$2

oarsh $CONTACT_HOST "cd HyFlexChain/hyflexchain && docker logs replica-0 2>&1 | grep -e 'block_time_sent' > $folder/block_sent.log"

for ((i=0;i<$NODES_PER_HOST;i++));
do
    for HOST in $HOSTS;
    do
        oarsh $HOST "cd HyFlexChain/hyflexchain && docker logs replica-$NODE_IDX 2>&1 | grep -e 'block_time_received' > $folder/block_received-$NODE_IDX.log"

        NODE_IDX=$(($NODE_IDX + 1));
    done;
done

echo "Done"
sleep 5