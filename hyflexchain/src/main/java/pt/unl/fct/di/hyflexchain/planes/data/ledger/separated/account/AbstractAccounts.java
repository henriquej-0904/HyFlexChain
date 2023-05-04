package pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.account;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;

/**
 * An abstract class that implements the Accounts interface and defines
 * abstract methods for changing state.
 */
public abstract class AbstractAccounts implements Accounts
{
	/**
	 * The consensus mechanism.
	 */
	protected final ConsensusMechanism consensus;

	/**
	 * Create a new object
	 * @param consensus The consensus mechanism
	 */
	protected AbstractAccounts(ConsensusMechanism consensus)
	{
		this.consensus = consensus;
	}

	/**
	 * Begin a new state change by adding an ordered block
	 * to the chain.
	 * The method {@link #processNewTransaction(HyFlexChainTransaction)}
	 * is called for each transaction on the block and
	 * any state changes become pending.
	 * Afterwards, the methods {@link #commitStateChange()}
	 * or {@link #abortStateChange()} must be called
	 * upon success or failure, respectively.
	 */
	protected abstract void beginStateChange();

	/**
	 * Upon a previous call to {@link #beginStateChange()},
	 * when calling this method all pending state changes
	 * are to be committed to the ledger.
	 */
	protected abstract void commitStateChange();

	/**
	 * Upon a previous call to {@link #beginStateChange()},
	 * when calling this method all pending state changes
	 * are to be discarded, thus reverting all pending changes,
	 * if any.
	 */
	protected abstract void abortStateChange();

	/**
	 * Process a new transaction and apply all corresponding changes:<p>
	 * 1) Add this tx to the sent transactions of the sender;<p>
	 * 2) Verify the validity of all input UTXO declared by the sender.
	 * If any of them are invalid, return false. If all are valid,
	 * invalidate them for future use.<p>
	 * 3) Verify all created UTXO and add mark them as valid for
	 * each corresponding destination account.
	 * 
	 * @param block The block where this transaction is inserted
	 * @param tx The transaction to process
	 * @return true if processed successfully.
	 */
	protected abstract boolean processNewTransaction(HyFlexChainBlock block,
		HyFlexChainTransaction tx);

	@Override
	public ConsensusMechanism getConsensusMechanism() {
		return this.consensus;
	}

	@Override
	public boolean processNewBlock(HyFlexChainBlock block)
	{
		this.beginStateChange();
		boolean result = true;

		var it = block.body().getTransactions().values().iterator();

		while (result && it.hasNext()) {
			result = processNewTransaction(block, it.next());
		}

		if (result)
			this.commitStateChange();
		else
			this.abortStateChange();

		return result;
	}

	
}
