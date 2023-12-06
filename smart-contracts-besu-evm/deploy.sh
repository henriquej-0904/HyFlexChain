#! /bin/bash

./build.sh

mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file  \
    -Dfile=./target/hyflexchain-evm-jar-with-dependencies.jar \
    -DgroupId=pt.unl.fct.di.hyflexchain -DartifactId=hyflexchain-evm \
    -Dversion=1.0-SNAPSHOT -Dpackaging=jar \
    -DlocalRepositoryPath=../dependencies/