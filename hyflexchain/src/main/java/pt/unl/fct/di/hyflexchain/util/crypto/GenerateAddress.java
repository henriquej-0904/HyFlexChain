package pt.unl.fct.di.hyflexchain.util.crypto;

import java.io.File;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.util.Utils;

/**
 * Generate an address from a key pair
 */
public class GenerateAddress {

    /**
     * 
     * @param args
     * @throws KeyStoreException
     * @throws JsonProcessingException
     */
    public static void main(String[] args) throws KeyStoreException, JsonProcessingException
    {
        File keystoreFile = new File(args[0]);
        String password = args[1];
        String type = args[2];

        KeyStore ks = Crypto.getKeyStore(keystoreFile, password, type);
        var entries = ks.aliases();

        String entry;
        PublicKey key;

        Map<Address, String> map = new LinkedHashMap<>();

        while (entries.hasMoreElements()) {
            entry = entries.nextElement();
            
            if (!ks.isCertificateEntry(entry))
                continue;

            key = ks.getCertificate(entry).getPublicKey();
            map.put(Address.fromPubKey(key), entry);

            // System.out.printf("%s:\t%s\n", entry, Address.fromPubKey(key).address());
        }

        System.out.println(Utils.json.writeValueAsString(map));
    }
}
