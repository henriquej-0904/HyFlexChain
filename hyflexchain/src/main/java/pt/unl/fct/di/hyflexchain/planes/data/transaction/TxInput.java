package pt.unl.fct.di.hyflexchain.planes.data.transaction;

import java.security.Signature;
import java.security.SignatureException;

import pt.unl.fct.di.hyflexchain.util.BytesOps;
import pt.unl.fct.di.hyflexchain.util.Utils;
import pt.unl.fct.di.hyflexchain.util.crypto.SignatureOps;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

/**
 * Represents a Transaction Input, aka a reference to a UTXO.
 * 
 * @param txId Pointer (address) to the transaction containing the UTXO to be spent
 * @param outputIndex The index number of the UTXO to be spent; first one is 0
 */
public record TxInput (
	byte[] txId, int outputIndex
) implements BytesOps, SignatureOps
{
	public static final ISerializer<TxInput> SERIALIZER =
		Utils.serializer.getRecordSerializer(
			TxInput.class,
			Utils.serializer.getArraySerializerByte(),
			Utils.serializer.getSerializer(int.class)
		);

	@Override
	public Signature update(Signature sig) throws SignatureException {
		sig.update(txId);
		sig.update(Utils.toBytes(outputIndex));
		return sig;
	}

	@Override
	public int serializedSize() {
		return BytesOps.serializedSize(txId)
			+ Integer.BYTES;
	}
}
