package pt.unl.fct.di.hyflexchain.planes.data.transaction;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import pt.unl.fct.di.hyflexchain.util.Bytes;

/**
 * Represents a Transaction Input, aka a reference to a UTXO.
 * 
 * @param txId Pointer to the transaction containing the UTXO to be spent
 * @param outputIndex The index number of the UTXO to be spent; first one is 0
 */
public record TxInput (
	TransactionId txId, int outputIndex
) implements Bytes<TxInput>
{

	@Override
	public void applyToBytes(Consumer<ByteBuffer> apply) {
		txId.applyToBytes(apply);
		apply.accept(ByteBuffer.allocate(Integer.BYTES).putInt(outputIndex));
	}
}
