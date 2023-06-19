package pt.unl.fct.di.hyflexchain.planes.data.transaction;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import pt.unl.fct.di.hyflexchain.util.Bytes;
import pt.unl.fct.di.hyflexchain.util.Utils;

/**
 * Represents an Unspent Transaction Output
 * 
 * @param sender The address of the receiving account
 * @param value The value to transfer
 */
public record UTXO(Address recipient, long value)
implements Bytes<UTXO>
{

	@Override
	public void applyToBytes(Consumer<ByteBuffer> apply) {
		apply.accept(ByteBuffer.wrap(recipient.address().getBytes()));
		apply.accept(Utils.toBytes(value));
	}
}
