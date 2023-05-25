package pt.unl.fct.di.hyflexchain.planes.data.transaction;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import pt.unl.fct.di.hyflexchain.util.Crypto;

/**
 * Represents an address in a transaction.
 * The address contains the algorithm used to generate
 * the key pair and the encoded public key.
 * 
 * <p>
 * The format string of an address is:
 * {@code algorithm;hex(encoded_public_key)},
 * being {@code hex()} a function to convert byte[]
 * to an hexadecimal string.
 */
public record Address(String address) {
	
	public PublicKey readPublicKey()
		throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		int algIndex = address.indexOf(';');

		if (algIndex <= 0)
			throw new InvalidKeySpecException("The address is malformed: " + address);

		String alg = address.substring(0, algIndex);
		
		if (address.length() <= algIndex + 1)
			throw new InvalidKeySpecException("The address is malformed: " + address);

		String encodedKey = address.substring(algIndex + 1);

		return Crypto.getPublicKey(encodedKey, alg);
	}

	public static Address fromPubKey(PublicKey key)
	{
		String alg = key.getAlgorithm();
		String encodedKey = Crypto.encodePublicKey(key);
		return new Address( alg + ";" + encodedKey );
	}

}
