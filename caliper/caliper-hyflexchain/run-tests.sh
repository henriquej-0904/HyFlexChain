#! /bin/bash

N_RUNS=$1
results_folder=$2

echo "Begin series of $N_RUNS tests..."

for ((i=0;i<$N_RUNS;i++));
do
    echo "Starting Iteration - $i"
    sleep 10
    ./run-test.sh $i $results_folder;
done

echo "Completed series of $N_RUNS tests."
