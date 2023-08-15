package pt.unl.fct.di.hyflexchain.planes.data.transaction;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import org.apache.tuweni.bytes.Bytes;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.BytesOps;
import pt.unl.fct.di.hyflexchain.util.Utils;
import pt.unl.fct.di.hyflexchain.util.crypto.Crypto;
import pt.unl.fct.di.hyflexchain.util.crypto.CryptoAlgorithm;
import pt.unl.fct.di.hyflexchain.util.crypto.HashOps;
import pt.unl.fct.di.hyflexchain.util.crypto.SignatureOps;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

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
public record Address(Bytes address) implements BytesOps, HashOps, SignatureOps {

	public static final Serializer SERIALIZER = new Serializer();

	public static final Address NULL_ADDRESS = new Address(Bytes.EMPTY);

	public static Address fromHexString(String address)
	{
		return new Address(Bytes.fromHexString(address));
	}

	public static Address fromBase64String(String address)
	{
		return new Address(Bytes.fromBase64String(address));
	}

	public String toHexString()
	{
		return address.toHexString();
	}

	public String toBase64String()
	{
		return address.toBase64String();
	}


	public PublicKey readPublicKey() throws InvalidAddressException
	{
		try {
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

		return new Address(Bytes.wrap(algId, encodedKey));
	}

	public boolean isNullAddress()
	{
		return this.equals(NULL_ADDRESS);
	}

	@Override
	public int hashCode() {
		return address.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Address other = (Address) obj;
		return address.equals(other.address);
	}



	public static class Serializer implements ISerializer<Address> {

		protected ISerializer<byte[]> byteArraySerializer =
            Utils.serializer.getArraySerializerByte();

		@Override
		public void serialize(Address t, ByteBuf out) throws IOException {
			byteArraySerializer.serialize(t.address.toArray(), out);
		}

		@Override
		public Address deserialize(ByteBuf in) throws IOException {
			byte[] array = byteArraySerializer.deserialize(in);
			return array.length == 0 ? NULL_ADDRESS : new Address(Bytes.wrap(array));
		}

		public int serializedSize(Address t)
		{
			return Integer.BYTES + t.address.size();
		}
	}



	@Override
	public Signature update(Signature sig) throws SignatureException {
		sig.update(address.toArrayUnsafe());
		return sig;
	}

	@Override
	public MessageDigest update(MessageDigest md) {
		md.update(Utils.toBytes(address.size()));
		md.update(address.toArrayUnsafe());
		return md;
	}

	@Override
	public int serializedSize()
	{
		return Integer.BYTES + address.size();
	}

}
