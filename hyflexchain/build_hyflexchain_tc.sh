#! /bin/bash

mvn clean compile assembly:single
docker build -f Dockerfile_tc -t henriquej0904/hyflexchain:hyflexchain-tc .
