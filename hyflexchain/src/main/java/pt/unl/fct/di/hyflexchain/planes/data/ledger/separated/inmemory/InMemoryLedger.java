package pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.inmemory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.BlockFilter;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockState;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.JsonLedgerState;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.LedgerState;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.ConsensusSpecificLedger;
import pt.unl.fct.di.hyflexchain.util.collections.UtilLists;
import pt.unl.fct.di.hyflexchain.util.config.LedgerConfig;

/**
 * An implementation of the Separated Ledger using
 * an in memory approach
 */
public class InMemoryLedger implements ConsensusSpecificLedger
{
	/**
	 * The consensus mechanism used by this ledger.
	 */
	protected final ConsensusMechanism consensus;

	/**
	 * The blockchain: a map that preserves insertion order
	 * and for each key (Block Id) corresponds a Block.
	 */
	protected final InMemoryBlockchain blockchain;
	// protected final OrderedMap<String, HyFlexChainBlock> blockchain;

	protected final List<Consumer<HyFlexChainBlock>> uponNewBlock;


	/**
	 * Create a new instance of the ledger for a specific consensus
	 * mechanism
	 * @param consensus The consensus mechanism used by this ledger.
	 */
	public InMemoryLedger(ConsensusMechanism consensus) {
		this.consensus = consensus;
		// this.blockchain = new LinkedMap<>(expectedBlockchainSize);
		this.blockchain = new InMemoryBlockchain();
		this.uponNewBlock = new ArrayList<>(10);
	}

	/**
	 * Create a new instance of the ledger for a specific consensus
	 * mechanism
	 * @param consensus The consensus mechanism used by this ledger.
	 * @param expectedSize The expected size of the blockchain.
	 */
	public InMemoryLedger(ConsensusMechanism consensus, int expectedBlockchainSize) {
		this.consensus = consensus;
		// this.blockchain = new LinkedMap<>(expectedBlockchainSize);
		this.blockchain = new InMemoryBlockchain(expectedBlockchainSize);
		this.uponNewBlock = new ArrayList<>(10);
	}

	@Override
	public InMemoryLedger init(LedgerConfig config, HyFlexChainBlock genesisBlock)
	{
		this.blockchain.put(genesisBlock.header().getMetaHeader().getHash(), genesisBlock);

		return this;
	}

	@Override
	public synchronized void writeOrderedBlock(HyFlexChainBlock block)
	{
		this.blockchain.put(block.header().getMetaHeader().getHash(), block);
		this.uponNewBlock.forEach((consumer) -> consumer.accept(block));
	}

	@Override
	public final ConsensusMechanism getConsensusMechanism() {
		return this.consensus;
	}

	@Override
	public synchronized Optional<HyFlexChainBlock> getBlock(String id) {
		return Optional.ofNullable(this.blockchain.get(id));
	}

	@Override
	public synchronized HyFlexChainBlock getLastBlock() {
		return this.blockchain.valuesList().listIterator(this.blockchain.size())
			.previous();
	}

	@Override
	public synchronized Optional<BlockState> getBlockState(String id) {
		return getBlock(id).map((block) -> BlockState.FINALIZED);
	}

	@Override
	public synchronized List<HyFlexChainBlock> getBlocks(BlockFilter filter)
	{
		//TODO: Check block filter

		List<HyFlexChainBlock> res = List.of();

		switch (filter.getType()) {
			case LAST_N:
				res = getLastBlocks((int) filter.getValue());
				break;
			case TIME_INTERVAL:
				throw new UnsupportedOperationException("Unsuppported Block filter: Time interval");
			case VALUE:
			throw new UnsupportedOperationException("Unsuppported Block filter: Value");
		}

		return res;
	}

	protected List<HyFlexChainBlock> getLastBlocks(int n)
	{
		return List.copyOf(UtilLists.subListLastElems(this.blockchain.valuesList(), n));
	}

	@Override
	public LedgerState getLedger()
	{
		var list = this.blockchain.valuesList();
		int size = list.size();

		return new JsonLedgerState(list.subList(0, size));
	}

	@Override
	public void uponNewBlock(Consumer<HyFlexChainBlock> action) {
		this.uponNewBlock.add(action);
	}

	@Override
	public int blockchainSize() {
		return this.blockchain.size();
	}

	@Override
	public void writeOrderedCommitteeBlock(HyFlexChainBlock block, Committee committee) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'writeOrderedCommitteeBlock'");
	}

	@Override
	public void uponNewCommitteeBlock(Consumer<HyFlexChainBlock> action) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'uponNewCommitteeBlock'");
	}
}
