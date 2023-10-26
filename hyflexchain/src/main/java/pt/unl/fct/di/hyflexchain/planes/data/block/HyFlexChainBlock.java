package pt.unl.fct.di.hyflexchain.planes.data.block;

import java.io.IOException;
import java.util.EnumMap;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.CommitteeId;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.SmartContract;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HashedTx;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionType;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TxInput;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.UTXO;
import pt.unl.fct.di.hyflexchain.util.BytesOps;
import pt.unl.fct.di.hyflexchain.util.crypto.Crypto;
import pt.unl.fct.di.hyflexchain.util.crypto.HashedObject;
import pt.unl.fct.di.hyflexchain.util.crypto.HyFlexChainSignature;
import pt.unl.fct.di.hyflexchain.util.crypto.SignatureAlgorithm;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

/**
 * Represents a block of transactions with all necessary metadata.
 * @param size The number of bytes that follows this field
 * @param header The header of this block
 * @param body The body of this block
 */
public record HyFlexChainBlock(
	int size, BlockHeader header, BlockBody body
) implements BytesOps
{
	protected static final Logger LOG = LoggerFactory.getLogger(HyFlexChainBlock.class);

	public final static Serializer SERIALIZER = new Serializer();

	public final static ISerializer<HashedObject<HyFlexChainBlock>> HASHED_BLOCK_SERIALIZER =
		HashedObject.createSerializer(SERIALIZER);


	public static final EnumMap<ConsensusMechanism, HashedObject<HyFlexChainBlock>>
		GENESIS_BLOCK = Stream.of(ConsensusMechanism.values())
			.map(HyFlexChainBlock::genesisBlock)
			.collect(Collectors.toMap(
				(b) -> b.obj().header().getMetaHeader().getConsensus(),
				UnaryOperator.identity(),
				(b1, b2) -> b1,
				() -> new EnumMap<>(ConsensusMechanism.class)
			));

	public static HashedObject<HyFlexChainBlock> genesisBlock(ConsensusMechanism consensus) {

		HyFlexChainTransaction tx =
			new HyFlexChainTransaction(
				HyFlexChainTransaction.Version.V1_0.toString(),
				Address.NULL_ADDRESS,
				SignatureAlgorithm.INVALID,
				"genesis signature".getBytes(),
				0,
				TransactionType.TRANSFER,
				SmartContract.reference(Address.NULL_ADDRESS),
				new TxInput[0],
				new UTXO[0],
				"GENESIS BLOCK -> HERE WE GO!!!".getBytes()
			);

		BlockBody body = BlockBody.from(HashedTx.from(tx));

		BlockMetaHeader metaHeader =
			new BlockMetaHeader(consensus, 0, new HyFlexChainSignature[0],
			CommitteeId.FIRST_COMMITTEE_ID);
		BlockHeader header = BlockHeader.create(metaHeader, Bytes.EMPTY,
			body.createMerkleTree().getRoot().hash(), 0);

		HyFlexChainBlock block = new HyFlexChainBlock(
			header.serializedSize() + body.serializedSize(),
			header, body
		);

		byte[] serializedBlock = new byte[block.serializedSize()];
		try {
			SERIALIZER.serialize(block, Unpooled.wrappedBuffer(serializedBlock).setIndex(0, 0));
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error(e.getMessage(), e);
		}
		return new HashedObject<>(Bytes.wrap(Crypto.getSha256Digest().digest(serializedBlock)), block);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof HyFlexChainBlock))
			return false;

		return this.header.getPrevHash()
			.equals(((HyFlexChainBlock)obj).header.getPrevHash());
	}

	@Override
	public int hashCode() {
		return this.header.getPrevHash().hashCode();
	}

	@Override
	public int serializedSize() {
		return Integer.BYTES + size;
	}

	public static int serializedSize(int headerSize, int bodySize)
	{
		return Integer.BYTES + headerSize + bodySize;
	}

	public static class Serializer implements ISerializer<HyFlexChainBlock>
	{

		@Override
		public void serialize(HyFlexChainBlock t, ByteBuf out) throws IOException {
			out.writeInt(t.size);
			BlockHeader.SERIALIZER.serialize(t.header, out);
			BlockBody.SERIALIZER.serialize(t.body, out);
		}

		@Override
		public HyFlexChainBlock deserialize(ByteBuf in) throws IOException {
			return new HyFlexChainBlock(
				in.readInt(),
				BlockHeader.SERIALIZER.deserialize(in),
				BlockBody.SERIALIZER.deserialize(in));
		}
		
	}

	
}
