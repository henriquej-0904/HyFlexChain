package pt.unl.fct.di.hyflexchain.planes.application.lvi.consensus;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;

import java.util.Map.Entry;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.BlockFilter;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.CommitteeId;
import pt.unl.fct.di.hyflexchain.planes.data.DataPlane;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockState;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.LedgerState;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionState;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.TransactionManagement;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.txpool.TxPool;
import pt.unl.fct.di.hyflexchain.util.ResetInterface;
import pt.unl.fct.di.hyflexchain.util.crypto.HashedObject;

public class LedgerViewConsensusImpl implements LedgerViewConsensusInterface, ResetInterface {

	protected final ConsensusMechanism consensus;

	protected final Map<Bytes, HyFlexChainTransaction> finalizedTxs;

	protected TxPool txPool;

	protected final DataPlane data;

	public LedgerViewConsensusImpl(ConsensusMechanism consensus) {
		this(consensus, Collections.synchronizedMap(new HashMap<>(100_000)));
	}

	/**
	 * @param consensus
	 * @param finalizedTxs
	 */
	protected LedgerViewConsensusImpl(ConsensusMechanism consensus, Map<Bytes, HyFlexChainTransaction> finalizedTxs) {
		this.consensus = consensus;
		this.finalizedTxs = finalizedTxs;

		this.data = DataPlane.getInstance();
		this.data.uponNewBlock((block) ->
			this.finalizedTxs.putAll(block.obj().body().findTransactions()), consensus);

	}

	@Override
	public void reset() {
		this.finalizedTxs.clear();
		this.data.uponNewBlock((block) ->
			this.finalizedTxs.putAll(block.obj().body().findTransactions()), consensus);
	}

	@Override
	public ConsensusMechanism getConsensusMechanism() {
		return consensus;
	}

	@Override
	public Optional<HyFlexChainTransaction> getTransaction(Bytes id) {
		return Optional.ofNullable(this.finalizedTxs.get(id));
	}

	@Override
	public TransactionState getTransactionState(Bytes id) {
		if (getTxPool().txExists(id))
			return TransactionState.PENDING;

		if (this.finalizedTxs.containsKey(id))
			return TransactionState.FINALIZED;

		return TransactionState.NOT_FOUND;
	}

	@Override
	public Optional<HyFlexChainBlock> getBlock(Bytes id) {
		return this.data.getBlock(id, consensus);
	}

	@Override
	public Optional<BlockState> getBlockState(Bytes id) {
		return this.data.getBlockState(id, consensus);
	}

	@Override
	public List<HashedObject<HyFlexChainBlock>> getBlocks(BlockFilter filter) {
		return this.data.getBlocks(filter, consensus);
	}

	@Override
	public LedgerState getLedger() {
		return this.data.getLedger(consensus);
	}

	protected TxPool getTxPool()
	{
		if (this.txPool == null)
			this.txPool = TransactionManagement.getInstance().getTxPool(consensus);

		return this.txPool;
	}

	@Override
	public Optional<Entry<CommitteeId, ? extends Committee>> getActiveCommittee() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getActiveCommittee'");
	}

	@Override
	public List<? extends Committee> getLedgerViewPreviousCommittees(int lastN) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getLedgerViewPreviousCommittees'");
	}

	@Override
	public Optional<Entry<CommitteeId, ? extends Committee>> getNextCommittee() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getNextCommittee'");
	}
	
}
