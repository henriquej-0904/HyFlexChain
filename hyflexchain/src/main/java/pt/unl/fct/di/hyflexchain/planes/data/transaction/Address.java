package pt.unl.fct.di.hyflexchain.planes.data.transaction;

import java.nio.ByteBuffer;
import java.security.PublicKey;
import org.apache.tuweni.bytes.Bytes;

import pt.unl.fct.di.hyflexchain.util.crypto.Crypto;
import pt.unl.fct.di.hyflexchain.util.crypto.CryptoAlgorithm;
import pt.unl.fct.di.hyflexchain.util.serializers.BytesSerializer;

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

	public static final BytesSerializer<Address> SERIALIZER = new Serializer();

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

	public static class Serializer implements BytesSerializer<Address> {

		protected BytesSerializer<byte[]> byteArraySerializer =
			BytesSerializer.primitiveArraySerializer(byte[].class);

		@Override
		public int serializedSize(Address obj) {
			return Integer.BYTES + obj.address.length() / 2;
		}

		@Override
		public ByteBuffer serialize(Address obj, ByteBuffer buff) {
			return byteArraySerializer.serialize(
				Bytes.fromHexString(obj.address).toArrayUnsafe(),
				buff
			);
		}

		@Override
		public Address deserialize(ByteBuffer buff) {
			return new Address(
				Bytes.wrap(byteArraySerializer.deserialize(buff)).toHexString()
			);
		}

		@Override
		public Class<Address> getType() {
			return Address.class;
		}
	}

}
