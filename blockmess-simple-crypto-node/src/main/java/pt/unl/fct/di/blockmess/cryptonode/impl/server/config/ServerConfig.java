package pt.unl.fct.di.blockmess.cryptonode.impl.server.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.SSLContext;

import jakarta.ws.rs.InternalServerErrorException;
import pt.unl.fct.di.blockmess.cryptonode.util.Crypto;

public class ServerConfig
{
    public static final String SERVER_CONFIG_FILE_NAME = "server-config.properties";

    private static KeyPair keyPair;
    private static KeyStore keyStore;
    private static KeyStore trustStore;
    private static Map<String, PublicKey> replicasKeys;

    private static int replicaId, blockmessPort;
    private static String replicaName;
    private static File replicaConfigFolder;

    public static void init(int replicaId, int blockmessPort) throws FileNotFoundException, IOException
    {
        ServerConfig.replicaId = replicaId;
        ServerConfig.blockmessPort = blockmessPort;
        ServerConfig.replicaName = "replica-" + replicaId;
        replicaConfigFolder = new File(Crypto.CONFIG_FOLDER, ServerConfig.replicaName);
    }

    /**
     * @return the blockmessPort
     */
    public static int getBlockmessPort() {
        return blockmessPort;
    }

    /**
     * @return the replicaId
     */
    public static int getReplicaId() {
        return replicaId;
    }

    public static String getReplicaName() {
        return replicaName;
    }

    public static KeyPair getKeyPair() {
        if(keyPair == null){
            try {
                keyPair = new KeyPair(getKeyStore().getCertificate(replicaName).getPublicKey(), 
                                     (PrivateKey) getKeyStore().getKey(replicaName, Crypto.KEYSTORE_PWD.toCharArray()));

            } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
                throw new Error(e.getMessage(), e);
            }
        }
        return keyPair;
    }

    public static KeyStore getKeyStore() {
        if(keyStore == null){
            keyStore = Crypto.getKeyStorePkcs12(new File(replicaConfigFolder, "keystore.pkcs12"), Crypto.KEYSTORE_PWD);
        }
        return keyStore;
    }

    public static KeyStore getTrustStore() {
        if(trustStore == null){
            trustStore = Crypto.getTrustStore();
        }
        return trustStore;
    }

    public static Map<String, PublicKey> getAllReplicaKeys()
    {
        if (replicasKeys != null)
            return replicasKeys;
        
        try
        {
            KeyStore truststore = getTrustStore();
            Iterator<String> ids = truststore.aliases().asIterator();
            Map<String, PublicKey> keys = new HashMap<>();

            while (ids.hasNext())
            {
                String id = ids.next();
                keys.put(id, truststore.getCertificate(id).getPublicKey());
            }

            replicasKeys = Map.copyOf(keys);
            return replicasKeys;

        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    public static SSLContext getSSLContext()
	{
		return Crypto.getSSLContext(getKeyStore(), getTrustStore(), Crypto.KEYSTORE_PWD);
	}
}
