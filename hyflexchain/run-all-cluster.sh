#! /bin/bash

# run multiple servers in a cluster

NUM_NODES=20
CONTACT_HOST=$(oarprint host | sort | head -n 1)
HOSTS=$(oarprint host | sort | head -n 4)
NUM_HOSTS=$(oarprint host | sort | head -n 4 | wc -l)
NODES_PER_HOST=$(($NUM_NODES / $NUM_HOSTS))

echo "Starting Blockmess Servers"

NODE_IDX=0

for ((i=0;i<$NODES_PER_HOST;i++));
do
    for HOST in $HOSTS;
    do
        oarsh $HOST "cd HyFlexChain/hyflexchain && ./run-blockmess_server-cluster.sh $NODE_IDX"

        if [ $NODE_IDX -eq 0 ]; then
            sleep 7;
        fi

        NODE_IDX=$(($NODE_IDX + 1));
    done;
done

echo "Done"
sleep 5

#########################################################################

echo -e "\nStarting HyFlexChain servers"

NODE_IDX=0

for ((i=0;i<$NODES_PER_HOST;i++));
do
    for HOST in $HOSTS;
    do
        oarsh $HOST "cd HyFlexChain/hyflexchain && ./run-server-cluster.sh $NODE_IDX"

        if [ $NODE_IDX -eq 0 ]; then
            sleep 7;
        fi

        NODE_IDX=$(($NODE_IDX + 1));
    done;
done

echo "Done"
sleep 5
