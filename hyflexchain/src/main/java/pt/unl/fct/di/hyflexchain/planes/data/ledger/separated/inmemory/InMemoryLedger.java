package pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.inmemory;

import java.util.Optional;
import java.util.Set;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.BlockFilter;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewConsensusInterface;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.TransactionFilter;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.views.HistoryPreviousCommittees;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.views.UTXOset;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockState;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.LedgerState;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.ConsensusSpecificLedger;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionState;
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
	 * Create a new instance of the ledger for a specific consensus
	 * mechanism
	 * @param consensus The consensus mechanism used by this ledger.
	 */
	public InMemoryLedger(ConsensusMechanism consensus) {
		this.consensus = consensus;
	}

	@Override
	public InMemoryLedger init(LedgerConfig config)
	{
		//TODO: add init to in memory ledger
		return this;
	}

	@Override
	public ConsensusMechanism getConsensusMechanism() {
		return this.consensus;
	}

	@Override
	public void writeOrderedBlock(HyFlexChainBlock block) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'writeOrderedBlock'");
	}

	@Override
	public Optional<HyFlexChainTransaction> getTransaction(String id) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getTransaction'");
	}

	@Override
	public Optional<TransactionState> getTransactionState(String id) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getTransactionState'");
	}

	@Override
	public Set<HyFlexChainTransaction> getTransactionsByOriginAccount(String originPubKey) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getTransactionsByOriginAccount'");
	}

	@Override
	public Set<HyFlexChainTransaction> getTransactionsByDestAccount(String destPubKey) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getTransactionsByDestAccount'");
	}

	@Override
	public Set<HyFlexChainTransaction> getTransactionsByOriginAccount(String originPubKey,
			pt.unl.fct.di.hyflexchain.planes.data.TransactionFilter filter) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getTransactionsByOriginAccount'");
	}

	@Override
	public Set<HyFlexChainTransaction> getTransactionsByDestAccount(String destPubKey,
			pt.unl.fct.di.hyflexchain.planes.data.TransactionFilter filter) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getTransactionsByDestAccount'");
	}

	@Override
	public Set<HyFlexChainTransaction> getTransactions(pt.unl.fct.di.hyflexchain.planes.data.TransactionFilter filter) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getTransactions'");
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
	public Set<HyFlexChainBlock> getBlocks(BlockFilter filter) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getBlocks'");
	}

	@Override
	public LedgerState getLedger() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getLedger'");
	}

	@Override
	public UTXOset getLedgerViewUTXOset() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getLedgerViewUTXOset'");
	}

	@Override
	public Committee getActiveCommittee() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getActiveCommittee'");
	}

	@Override
	public HistoryPreviousCommittees getLedgerViewPreviousCommittees(int lastN) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getLedgerViewPreviousCommittees'");
	}

	

	

	
	
}
