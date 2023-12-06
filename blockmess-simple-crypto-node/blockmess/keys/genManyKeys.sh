#!/bin/bash

START=$1
END=$2

if [ -z $START ] || [ -z $END ];
then
	echo "$0 <Starting Number> <Ending Number>"
	echo "Starting Number: The number associated with the first key to be generated."
	echo "Ending Number:   The number associated with the last key generated, inclusive."
	exit
fi

echo "Starting to generate keys"
for KEY_NUM in $(seq $START $END)
do
	./genKeyPair.sh $KEY_NUM
done
echo "Finished generating keys"
