#! /bin/bash

if [ -z $1 ];
then
	echo "Missing argument with the name of the keys"
	exit
fi
openssl ecparam -name secp256r1 -genkey -noout -out aux.pem
openssl ec -in aux.pem -pubout > public_$1.pem
openssl pkey -in aux.pem -out secret_$1.pem
rm aux.pem
