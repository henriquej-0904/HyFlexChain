package pt.unl.fct.di.hyflexchain.util.crypto;

import java.io.File;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;

/**
 * Generate an address from a key pair
 */
public class GenerateAddress {

    /**
     * 
     * @param args
     * @throws KeyStoreException
     */
    public static void main(String[] args) throws KeyStoreException
    {
        File keystoreFile = new File(args[0]);
        String password = args[1];
        String type = args[2];

        KeyStore ks = Crypto.getKeyStore(keystoreFile, password, type);
        var entries = ks.aliases();

        String entry;
        PublicKey key;

        while (entries.hasMoreElements()) {
            entry = entries.nextElement();
            
            if (!ks.isCertificateEntry(entry))
                continue;

            key = ks.getCertificate(entry).getPublicKey();
            System.out.printf("%s:\t%s\n", entry, Address.fromPubKey(key).address());
        }   
    }
}
