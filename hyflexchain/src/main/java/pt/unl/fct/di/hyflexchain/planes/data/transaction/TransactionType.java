package pt.unl.fct.di.hyflexchain.planes.data.transaction;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.BytesOps;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

/**
 * The different types of transactions.
 */
public enum TransactionType implements BytesOps {
	
	/**
	 * Regular transactions: a transaction that transfers assets/tokens.
	 */
	TRANSFER ((byte) 1),

	/**
	 * Contract deployment transactions: a transaction that creates
	 * and installs a smart contract on the chain. After being installed
	 * it can be referenced in future transactions for execution.
	 */
	CONTRACT_CREATE ((byte) 2),

	/**
	 * Revoke a contract previously installed on the chain.
	 * After this transaction is approved/executed it is no longer
	 * possible to reference and execute the revoked smart contract.
	 */
	CONTRACT_REVOKE ((byte) 3),

	/**
	 * An internal type used by HyFlexChain nodes to propose
	 * committees for the future.
	 */
	COMMITTEE_ELECTION ((byte) 4),

	/**
	 * An internal type used by HyFlexChain nodes to rotate
	 * the currently executing committee.
	 */
	COMMITTEE_ROTATION ((byte) 5);


	public static final Serializer SERIALIZER = new Serializer();

	public final byte id;

	/**
	 * @param id
	 */
	private TransactionType(byte id) {
		this.id = id;
	}

	public static Optional<TransactionType> decode(byte id)
    {
        return Stream.of(values())
            .filter((alg) -> alg.id == id)
            .findAny();
    }

    public static TransactionType decodeOrThrow(byte id)
    {
        return decode(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid transaction type identifier"));
    }

	static final class Serializer implements ISerializer<TransactionType>
    {
        @Override
        public void serialize(TransactionType t, ByteBuf out) throws IOException {
            out.writeByte(t.id);
        }

        @Override
        public TransactionType deserialize(ByteBuf in) throws IOException {
            try {
                return decodeOrThrow(in.readByte());
            } catch (Exception e) {
                throw new IOException(e.getMessage(), e);
            }
        }
    }

	@Override
	public int serializedSize() {
		return Byte.BYTES;
	}

}
