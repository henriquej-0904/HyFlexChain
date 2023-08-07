package pt.unl.fct.di.hyflexchain.planes.data.block;

import java.security.MessageDigest;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionType;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TxInput;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.UTXO;
import pt.unl.fct.di.hyflexchain.util.Utils;
import pt.unl.fct.di.hyflexchain.util.crypto.Crypto;
import pt.unl.fct.di.hyflexchain.util.crypto.HyflexchainSignature;

/**
 * Represents a block of transactions with all necessary metadata.
 */
public record HyFlexChainBlock(
	BlockHeader header, BlockBody body
)
{
	protected static final Logger LOG = LoggerFactory.getLogger(HyFlexChainBlock.class);

	public static final EnumMap<ConsensusMechanism, HyFlexChainBlock>
		GENESIS_BLOCK = Stream.of(ConsensusMechanism.values())
			.map(HyFlexChainBlock::genesisBlock)
			.collect(Collectors.toMap(
				(b) -> b.header().getMetaHeader().getConsensus(),
				(b) -> b,
				(b1, b2) -> b1,
				() -> new EnumMap<>(ConsensusMechanism.class)
			));

	public static HyFlexChainBlock genesisBlock(ConsensusMechanism consensus) {

		HyFlexChainTransaction tx = new HyFlexChainTransaction();
		tx.setVersion(HyFlexChainTransaction.Version.V1_0.toString());
		tx.setTransactionType(TransactionType.TRANSFER);
		tx.setSender(new Address("genesis address"));
		tx.setSignatureType("genesis signature alg");
		tx.setNonce(0);
		tx.setInputTxs(new TxInput[0]);
		tx.setOutputTxs(new UTXO[0]);
		tx.setData("GENESIS BLOCK -> HERE WE GO!!!".getBytes());
		tx.setSignature("genesis signature");
		tx.setHash(tx.hash());
		
		LinkedHashMap<String, HyFlexChainTransaction> txs =
			LinkedHashMap.newLinkedHashMap(1);
		txs.put(tx.getHash(), tx);

		BlockBody body = BlockBody.from(txs);

		BlockMetaHeader metaHeader =
			new BlockMetaHeader(consensus, 0, new HyflexchainSignature[0],
			0, "");
		BlockHeader header = BlockHeader.create(metaHeader, "genesis block",
			body.getMerkleTree().getRoot().hash(), 0);

		HyFlexChainBlock block = new HyFlexChainBlock(header, body);
		block.calcAndSetHash();

		return block;
	}
	
	/**
	 * Calculate the hash of this block
	 * and set it to the hash field of the header.
	 */
	public void calcAndSetHash()
	{
		header().getMetaHeader()
			.setHash(calcHash());
	}

	public String calcHash()
	{
		return Utils.toHex(
			calcHash(Crypto.getSha256Digest()).digest()
		);
	}

	public MessageDigest calcHash(MessageDigest msgDigest)
	{
		header().calcHash(msgDigest);
		body().calcHash(msgDigest);
		return msgDigest;
	}

	public boolean verifyHash()
	{
		var hash = header().getMetaHeader().getHash();

		if (hash == null)
			return false;

		return hash.equalsIgnoreCase(calcHash());
	}

	public boolean verifyBlock()
	{
		if (!verifyHash())
		{
			LOG.info("Block invalid hash.");
			return false;
		}

		if (! body().getMerkleTree().verifyTree(
			body().findTransactions().keySet()
		))
		{
			LOG.info("Block Merkle tree.");
			return false;
		}

		return true;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof HyFlexChainBlock))
			return false;

		return this.header.getMetaHeader().getHash()
			.equalsIgnoreCase(((HyFlexChainBlock)obj).header.getMetaHeader().getHash());
	}

	@Override
	public int hashCode() {
		return this.header.getMetaHeader().getHash().hashCode();
	}

	
}
