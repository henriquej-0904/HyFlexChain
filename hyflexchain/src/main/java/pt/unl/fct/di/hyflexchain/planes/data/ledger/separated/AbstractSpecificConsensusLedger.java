package pt.unl.fct.di.hyflexchain.planes.data.ledger.separated;

import java.util.List;
import java.util.Optional;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.views.HistoryPreviousCommittees;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.views.UTXOset;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.data.TransactionFilter;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockState;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.account.Accounts;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.blockchain.TxFinderRec;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.blockchain.TxFinderList;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionId;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionState;
import pt.unl.fct.di.hyflexchain.util.config.LedgerConfig;

public abstract class AbstractSpecificConsensusLedger implements ConsensusSpecificLedger
{
	/**
	 * The consensus mechanism used by this ledger.
	 */
	protected final ConsensusMechanism consensus;

	protected final Accounts accounts;

	protected Committee activeCommittee;

	/**
	 * @param consensus
	 * @param accounts
	 */
	public AbstractSpecificConsensusLedger(ConsensusMechanism consensus, Accounts accounts) {
		this.consensus = consensus;
		this.accounts = accounts;
	}

	/**
	 * Get a transaction.
	 * @param txFinder An object to locate a transaction
	 * @return The transaction.
	 */
	public abstract Optional<HyFlexChainTransaction> getTransaction(TxFinderRec txFinder);

	/**
	 * Get all requested transactions.
	 * @param txFinders A list of all requested transactions.
	 * @return All requested transactions.
	 */
	public abstract List<HyFlexChainTransaction> getTransactions(List<TxFinderList> txFinders);

	/**
	 * Append a block to the blockchain.
	 * @param block The block to append to the blockchain.
	 */
	public abstract void appendBlock(HyFlexChainBlock block);

	@Override
	public ConsensusSpecificLedger init(LedgerConfig config) {
		// TODO Impl init in Blockchain Class
		return this;
	}

	@Override
	public final void writeOrderedBlock(HyFlexChainBlock block) {
		this.accounts.processNewBlock(block);
		this.appendBlock(block);
	}

	@Override
	public final Committee getActiveCommittee() {
		return this.activeCommittee;
	}

	@Override
	public final ConsensusMechanism getConsensusMechanism() {
		return this.consensus;
	}

	@Override
	public final Optional<HyFlexChainTransaction> getTransaction(TransactionId id)
	{
		return this.accounts.locateTransaction(id)
			.flatMap(this::getTransaction);
	}


	@Override
	public final Optional<TransactionState> getTransactionState(TransactionId id)
	{
		return this.accounts.locateTransaction(id)
			.flatMap((txFinder) -> this.getBlockState(txFinder.blockHash()))
			.map(BlockState::toTransactionState);
	}

	@Override
	public final List<HyFlexChainTransaction> getTransactionsByDestAccount(String address)
	{
		return this.getTransactions(this.accounts.locateTransactionsByDestAccount(address));
	}

	@Override
	public final List<HyFlexChainTransaction> getTransactionsByOriginAccount(String address)
	{
		return this.getTransactions(this.accounts.locateTransactionsByOriginAccount(address));
	}

	@Override
	public HistoryPreviousCommittees getLedgerViewPreviousCommittees(int lastN) {
		throw new UnsupportedOperationException("getLedgerViewPreviousCommittees");
	}

	@Override
	public UTXOset getLedgerViewUTXOset() {
		throw new UnsupportedOperationException("getLedgerViewUTXOset");
	}

	@Override
	public List<HyFlexChainTransaction> getTransactions(TransactionFilter filter) {
		throw new UnsupportedOperationException("getTransactions");
	}

	@Override
	public List<HyFlexChainTransaction> getTransactionsByDestAccount(String address, TransactionFilter filter) {
		throw new UnsupportedOperationException("getTransactionsByDestAccount");
	}

	@Override
	public List<HyFlexChainTransaction> getTransactionsByOriginAccount(String address, TransactionFilter filter) {
		throw new UnsupportedOperationException("getTransactionsByOriginAccount");
	}

	
}
