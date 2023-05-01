package pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.inmemory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.BlockFilter;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.views.HistoryPreviousCommittees;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.views.UTXOset;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockState;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.LedgerState;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.ConsensusSpecificLedger;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.blockchain.Blockchain;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.blockchain.TxFinder;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.blockchain.TxFinderList;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionId;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionState;
import pt.unl.fct.di.hyflexchain.util.config.LedgerConfig;

/**
 * An implementation of the Separated Ledger using
 * an in memory approach
 */
public class InMemoryLedger extends Blockchain
{
	public final static int BLOCKCHAIN_INIT_SIZE = 1000;

	/**
	 * The blockchain: a map that preserves insertion order
	 * and for each key (Block Id) corresponds a Block.
	 */
	protected final Map<String, HyFlexChainBlock> blockchain;



	/**
	 * Create a new instance of the ledger for a specific consensus
	 * mechanism
	 * @param consensus The consensus mechanism used by this ledger.
	 */
	public InMemoryLedger(ConsensusMechanism consensus) {
		super(consensus, new InMemoryAccounts(consensus));

		this.blockchain = new LinkedHashMap<>(BLOCKCHAIN_INIT_SIZE);
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
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getBlock'");
	}

	@Override
	public Optional<BlockState> getBlockState(String id) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getBlockState'");
	}

	@Override
	public List<HyFlexChainBlock> getBlocks(BlockFilter filter) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getBlocks'");
	}

	@Override
	public LedgerState getLedger() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getLedger'");
	}
}
