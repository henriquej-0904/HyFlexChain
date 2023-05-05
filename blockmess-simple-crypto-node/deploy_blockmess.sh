#! /bin/bash

mvn deploy:deploy-file \
-DgroupId=blockmess \
-DartifactId=Blockmess_Simple_PoET \
-Dversion=1.0-SNAPSHOT \
-Durl=file:./blockmess/blockmess_lib/ \
-DrepositoryId=blockmess_lib \
-DupdateReleaseInfo=true \
-Dfile=../../Blockmess/target/BlockmessLib.jar