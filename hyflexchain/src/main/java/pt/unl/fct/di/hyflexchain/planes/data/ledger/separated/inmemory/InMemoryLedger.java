package pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.inmemory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.BlockFilter;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockState;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.JsonLedgerState;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.LedgerState;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.AbstractSpecificConsensusLedger;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.TxFinder;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.TxFinderList;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.util.config.LedgerConfig;

/**
 * An implementation of the Separated Ledger using
 * an in memory approach
 */
public class InMemoryLedger extends AbstractSpecificConsensusLedger
{
	/**
	 * The blockchain: a map that preserves insertion order
	 * and for each key (Block Id) corresponds a Block.
	 */
	protected final InMemoryBlockchain blockchain;


	/**
	 * Create a new instance of the ledger for a specific consensus
	 * mechanism
	 * @param consensus The consensus mechanism used by this ledger.
	 */
	public InMemoryLedger(ConsensusMechanism consensus) {
		super(consensus, new InMemoryAccounts(consensus));
		this.blockchain = new InMemoryBlockchain();
	}

	@Override
	public InMemoryLedger init(LedgerConfig config)
	{
		super.init(config);
		// TODO: init Memory Ledger

		return this;
	}

	@Override
	public void appendBlock(HyFlexChainBlock block) {
		this.blockchain.put(block.header().getMetaHeader().getHash(), block);
	}

	@Override
	public Optional<HyFlexChainTransaction> getTransaction(TxFinder txFinder)
	{
		var block = this.blockchain.get(txFinder.blockHash());
		if (block == null)
			return Optional.empty();

		return block.body().findTransaction(txFinder.txHash());
	}

	@Override
	public List<HyFlexChainTransaction> getTransactions(List<TxFinderList> txFinders)
	{
		List<List<HyFlexChainTransaction>> txList = txFinders.stream()
			.map((txFinderList) ->
				new ImmutablePair<>(txFinderList,
					this.blockchain.get(txFinderList.blockHash())
					)
			)
			.filter((p) -> p.right != null)
			.map((p) -> p.right.body().findTransactions(p.left.txHashes()))
			.toList();

		int total = txList.stream().mapToInt(List::size).sum();
		
		List<HyFlexChainTransaction> res = new ArrayList<>(total);
		for (List<HyFlexChainTransaction> list : txList) {
			res.addAll(list);
		}

		return res;
	}

	@Override
	public Optional<HyFlexChainBlock> getBlock(String id) {
		return Optional.ofNullable(this.blockchain.get(id));
	}

	@Override
	public Optional<BlockState> getBlockState(String id) {
		return getBlock(id).map((block) -> BlockState.FINALIZED);
	}

	@Override
	public List<HyFlexChainBlock> getBlocks(BlockFilter filter)
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
		int blockchainSize = this.blockchain.size();
		int toIndex = blockchainSize;
		int fromIndex = blockchainSize - n;
		
		if (fromIndex < 0)
		{
			fromIndex = 0;
		}

		return this.blockchain.valuesList().subList(fromIndex, toIndex);
	}

	@Override
	public LedgerState getLedger()
	{
		var list = this.blockchain.valuesList();
		int size = list.size();

		return new JsonLedgerState(list.subList(0, size));
	}
}
