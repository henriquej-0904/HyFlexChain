package pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.inmemory;

import java.util.List;
import java.util.Optional;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.views.UTXOset;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.account.AbstractAccounts;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.account.Accounts;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.blockchain.TxFinderRec;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.blockchain.TxFinderList;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionId;

public class InMemoryAccounts extends AbstractAccounts {

	

	/**
	 * @param consensus
	 */
	public InMemoryAccounts(ConsensusMechanism consensus) {
		super(consensus);
	}

	@Override
	public Optional<TxFinderRec> locateTransaction(TransactionId id) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'locateTransaction'");
	}

	@Override
	public List<TxFinderList> locateTransactionsByOriginAccount(String address) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'locateTransactionsByOriginAccount'");
	}

	@Override
	public List<TxFinderList> locateTransactionsByDestAccount(String address) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'locateTransactionsByDestAccount'");
	}

	@Override
	public UTXOset getLedgerViewUTXOset() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getLedgerViewUTXOset'");
	}

	@Override
	protected void beginStateChange() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'beginStateChange'");
	}

	@Override
	protected void commitStateChange() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'commitStateChange'");
	}

	@Override
	protected void abortStateChange() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'abortStateChange'");
	}

	@Override
	protected boolean processNewTransaction(HyFlexChainBlock block, HyFlexChainTransaction tx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'processNewTransaction'");
	}

	
	
}
