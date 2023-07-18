#! /bin/bash

# Script to create all needed addresses.

min_id=$1
max_id=$2

configFolder="./hyflexchain-config/keys"
truststore_root_ca=$configFolder/truststore_root_ca.pkcs12
hyflexchain_root_keystore=$configFolder/hyflexchain_root_keystore.pkcs12
hyflexchain_root_cert=$configFolder/hyflexchain_root_cert.pem

truststore_all=$configFolder/truststore_all_replicas.pkcs12

keystorepass="keystorepwd"

# rm -r $configFolder
mkdir -p $configFolder

# Create Hyflexchain root keypair & certificate

if [ ! -f $truststore_root_ca ]; then
    echo Create HyFlexChain root certificate
    keytool -genkeypair -groupname secp256r1 -sigalg SHA256withECDSA -keyalg EC -alias hyflexchain-root-ca -validity 365 -dname "CN=hyflexchain-ca,OU=hyflexchain-ca,O=hyflexchain-ca,L=Lisbon,ST=Lisbon,C=PT" -storetype pkcs12 -keystore $hyflexchain_root_keystore -storepass $keystorepass -keypass $keystorepass -ext bc=ca:true

    echo Export HyFlexChain root certificate
    keytool -exportcert -rfc -alias hyflexchain-root-ca -keystore $hyflexchain_root_keystore -storepass $keystorepass -file $hyflexchain_root_cert
    
    echo Create truststore_root_ca
    keytool -import -noprompt -alias hyflexchain-root-ca -file $hyflexchain_root_cert -keystore $truststore_root_ca -storepass $keystorepass -storetype pkcs12;
fi

# Create replica's keypairs & certificates

echo -----------------------------------------------------------------------------
echo Generating Key pairs and addresses...

for (( i=$min_id; i <= $max_id; i++ ));
do
    replicaAlias="replica-$i"
    replicaFolder=$configFolder/$replicaAlias

    rm -rf $replicaFolder

    echo ""
    echo $replicaAlias

    mkdir -p $replicaFolder

    # Generate key pair
    keytool -genkeypair -groupname secp256r1 -sigalg SHA256withECDSA -keyalg EC -alias $replicaAlias -validity 365 -dname "CN=hyflexchain,OU=hyflexchain,O=hyflexchain,L=Lisbon,ST=Lisbon,C=PT" -storetype pkcs12 -keystore $replicaFolder/keystore.pkcs12 -storepass $keystorepass -keypass $keystorepass

    # Generate replica certificate signed with hyflexchain root CA
    keytool -certreq -keystore $replicaFolder/keystore.pkcs12 -storepass $keystorepass -alias $replicaAlias -storetype pkcs12 | keytool -gencert -rfc -keystore $hyflexchain_root_keystore -storepass $keystorepass -alias hyflexchain-root-ca -storetype pkcs12 -outfile $replicaFolder/certificate.pem

    cat $hyflexchain_root_cert $replicaFolder/certificate.pem | keytool -import -noprompt -alias $replicaAlias -keystore $replicaFolder/keystore.pkcs12 -storepass $keystorepass -storetype pkcs12

    # Export certificate
    # keytool -export -alias $replicaAlias -keystore $replicaFolder/keystore.pkcs12 -storepass $keystorepass -file $replicaFolder/certificate.pem

    # Remove old entry
    keytool -delete -alias $replicaAlias -keystore $truststore_all -storepass $keystorepass -storetype pkcs12

    # Copy certificate to truststore to be used by the client
    keytool -import -noprompt -alias $replicaAlias -file $replicaFolder/certificate.pem -keystore $truststore_all -storepass $keystorepass -storetype pkcs12

done

java -cp target/hyflexchain-jar-with-dependencies.jar pt.unl.fct.di.hyflexchain.util.crypto.GenerateAddress $truststore_all $keystorepass PKCS12 > $configFolder/addresses.json

echo "Created configs with success"

cp $hyflexchain_root_cert ../caliper/caliper-hyflexchain/crypto/
echo "Copied hyflexchain_root_cert to caliper crypto config with success"


