#! /bin/bash

mkdir -p config
cp -r /app/blockmess-config/* /app/config
cp -r hyflexchain-config/bft_smart/config/* config

eval $@
