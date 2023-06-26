package pt.unl.fct.di.hyflexchain.planes.data.transaction;

import java.security.PublicKey;
import org.apache.tuweni.bytes.Bytes;
import pt.unl.fct.di.hyflexchain.util.crypto.Crypto;
import pt.unl.fct.di.hyflexchain.util.crypto.CryptoAlgorithm;

/**
 * Represents an address in a transaction.
 * The address contains the algorithm used to generate
 * the key pair and the encoded public key.
 * 
 * <p>
 * The format string of an address is: <p>
 * {@code hex( algorithmId;encoded_public_key )},
 * being {@code hex()} a function to convert byte[]
 * to an hexadecimal string.
 */
public record Address(String address) {

	public PublicKey readPublicKey() throws InvalidAddressException
	{
		try {
			Bytes address = Bytes.fromHexString(this.address);

			CryptoAlgorithm alg = CryptoAlgorithm.decodeOrThrow(address.get(0));
			byte[] encodedPublicKey = address.slice(1).toArray();
			
			return Crypto.getPublicKey(encodedPublicKey, alg.getName());
		} catch (Exception e) {
			throw new InvalidAddressException("The address is invalid.", e);
		}
	}

	public static Address fromPubKey(PublicKey key)
	{
		Bytes algId = Bytes.of(
			CryptoAlgorithm.fromPublicKey(key).getAlgId()
		);
		
		Bytes encodedKey = Bytes.wrap(Crypto.encodePublicKey(key));

		return new Address(Bytes.wrap(algId, encodedKey).toHexString());
	}

}
