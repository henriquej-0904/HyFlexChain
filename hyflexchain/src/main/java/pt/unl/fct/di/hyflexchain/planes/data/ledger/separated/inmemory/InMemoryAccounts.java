package pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.inmemory;

import java.util.List;
import java.util.Optional;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.views.UTXOset;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.account.Accounts;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.blockchain.TxFinder;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.blockchain.TxFinderList;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionId;

public class InMemoryAccounts implements Accounts {

	protected final ConsensusMechanism consensus;

	

	/**
	 * @param consensus
	 */
	public InMemoryAccounts(ConsensusMechanism consensus) {
		this.consensus = consensus;
	}

	@Override
	public ConsensusMechanism getConsensusMechanism() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getConsensusMechanism'");
	}

	@Override
	public void processNewBlock(HyFlexChainBlock block) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'processNewBlock'");
	}

	@Override
	public Optional<TxFinder> locateTransaction(TransactionId id) {
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
	
}
