package pt.unl.fct.di.hyflexchain.planes.data.transaction;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import pt.unl.fct.di.hyflexchain.util.Bytes;

/**
 * The transaction Id is based on the address
 * of the sender and the tx hash.
 * 
 * @param senderAddress The address
 * of the sender of the transaction
 * 
 * @param txHash The hash of the transaction
 */
public record TransactionId(String senderAddress, String txHash)
implements Bytes<TransactionId> {

	@Override
	public void applyToBytes(Consumer<ByteBuffer> apply) {
		apply.accept(ByteBuffer.wrap(senderAddress.getBytes()));
		apply.accept(ByteBuffer.wrap(txHash.getBytes()));
	}
}
