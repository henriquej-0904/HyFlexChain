#! /bin/bash

ENDPOINTS=20

for ((i=0; i < $ENDPOINTS; i++)); do cat networkconfig.json | jq ".hyflexchain.url[0] = \"https://localhost:`expr 18000 + $i`/api/rest\"" > networkconfig-$i.json; done

