#! /bin/bash

# Script to create all keystores and truststores needed.

nReplicas=$1
configFolder="./tls-config"
keystorepass="keystorepwd"

rm -r $configFolder
mkdir -p $configFolder

# Create replica's keypairs & certificates

echo -----------------------------------------------------------------------------
echo Creating TLS configs...

for (( i=0; i < $nReplicas; i++ ));
do

    replicaAlias="replica-$i"
    replicaFolder=$configFolder/$replicaAlias

    echo ""
    echo $replicaAlias

    mkdir -p $replicaFolder

    # Generate key pair
    keytool -genkeypair -groupname secp521r1 -sigalg SHA256withECDSA -keyalg EC -alias $replicaAlias -validity 365 -dname "CN=itdlp,OU=,O=itdlp,L=Lisbon,ST=Lisbon,C=PT" -storetype pkcs12 -keystore $replicaFolder/keystore.pkcs12 -storepass $keystorepass -keypass $keystorepass

    # Export certificate
    keytool -export -alias $replicaAlias -keystore $replicaFolder/keystore.pkcs12 -storepass $keystorepass -file $replicaFolder/certificate.pem

    # Copy certificate to truststore to be used by the client
    keytool -import -noprompt -alias $replicaAlias -file $replicaFolder/certificate.pem -keystore $configFolder/truststore.pkcs12 -storepass $keystorepass -storetype pkcs12

done

echo "Created configs with success"
