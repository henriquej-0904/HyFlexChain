package pt.unl.fct.di.hyflexchain.planes.data.transaction;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import pt.unl.fct.di.hyflexchain.util.Bytes;

/**
 * Represents an Unspent Transaction Output
 * 
 * @param address The address of the receiving account
 * @param value The value to transfer
 */
public record UTXO(String address, long value)
implements Bytes<UTXO>
{

	@Override
	public void applyToBytes(Consumer<ByteBuffer> apply) {
		apply.accept(ByteBuffer.wrap(address.getBytes()));
		apply.accept(ByteBuffer.allocate(Long.BYTES).putLong(value));
	}
}
