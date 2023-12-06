package pt.unl.fct.di.hyflexchain.planes.data.transaction;

import java.security.Signature;
import java.security.SignatureException;

import pt.unl.fct.di.hyflexchain.util.BytesOps;
import pt.unl.fct.di.hyflexchain.util.Utils;
import pt.unl.fct.di.hyflexchain.util.crypto.SignatureOps;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

/**
 * Represents an Unspent Transaction Output
 * 
 * @param recipient The address of the receiving account
 * @param value The value to transfer
 */
public record UTXO(Address recipient, long value) implements BytesOps, SignatureOps
{
	public static final ISerializer<UTXO> SERIALIZER =
		Utils.serializer.getRecordSerializer(
			UTXO.class,
			Address.SERIALIZER,
			Utils.serializer.getSerializer(long.class)
		);

	@Override
	public Signature update(Signature sig) throws SignatureException {
		recipient.update(sig);
		sig.update(Utils.toBytes(value));
		return sig;
	}

	@Override
	public int serializedSize() {
		return recipient.serializedSize() + Long.BYTES;
	}
	
}
