package pt.unl.fct.di.hyflexchain.planes.application.lvi.consensus;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.BlockFilter;
import pt.unl.fct.di.hyflexchain.planes.application.ti.InvalidTransactionException;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.data.DataPlane;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockState;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.LedgerState;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionState;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.TransactionManagement;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.txpool.TxPool;

public class LedgerViewConsensusImpl implements LedgerViewConsensusInterface {

	protected final ConsensusMechanism consensus;

	protected final Map<String, HyFlexChainTransaction> finalizedTxs;

	protected TxPool txPool;

	protected final DataPlane data;

	public LedgerViewConsensusImpl(ConsensusMechanism consensus) {
		this(consensus, Collections.synchronizedMap(new HashMap<>(100_000)));
	}

	/**
	 * @param consensus
	 * @param finalizedTxs
	 */
	public LedgerViewConsensusImpl(ConsensusMechanism consensus, Map<String, HyFlexChainTransaction> finalizedTxs) {
		this.consensus = consensus;
		this.finalizedTxs = finalizedTxs;

		this.data = DataPlane.getInstance();
		this.data.uponNewBlock((block) ->
			this.finalizedTxs.putAll(block.body().getTransactions()), consensus);

	}

	@Override
	public ConsensusMechanism getConsensusMechanism() {
		return consensus;
	}

	@Override
	public Optional<HyFlexChainTransaction> getTransaction(String id) {
		var tx = getTxPool().getPendingTx(id);

		if (tx.isPresent())
			return tx;

		var finalized = this.finalizedTxs.get(id);
		if (finalized != null)
			tx = Optional.of(finalized);

		return tx;
	}

	@Override
	public TransactionState getTransactionState(String id) {
		if (getTxPool().txExists(id))
			return TransactionState.PENDING;

		if (this.finalizedTxs.containsKey(id))
			return TransactionState.FINALIZED;

		return TransactionState.NOT_FOUND;
	}

	@Override
	public Optional<HyFlexChainBlock> getBlock(String id) {
		return this.data.getBlock(id, consensus);
	}

	@Override
	public Optional<BlockState> getBlockState(String id) {
		return this.data.getBlockState(id, consensus);
	}

	@Override
	public List<HyFlexChainBlock> getBlocks(BlockFilter filter) {
		return this.data.getBlocks(filter, consensus);
	}

	@Override
	public LedgerState getLedger() {
		return this.data.getLedger(consensus);
	}

	@Override
	public Committee getActiveCommittee() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getActiveCommittee'");
	}

	@Override
	public List<Committee> getLedgerViewPreviousCommittees(int lastN) {
		return this.data.getLedgerViewPreviousCommittees(lastN, consensus);
	}

	protected TxPool getTxPool()
	{
		if (this.txPool == null)
			try {
				this.txPool = TransactionManagement.getInstance().getTxPool(consensus);
			} catch (InvalidTransactionException e) {
				e.printStackTrace();
				throw new Error(e.getMessage(), e);
			}

		return this.txPool;
	}
	
}
