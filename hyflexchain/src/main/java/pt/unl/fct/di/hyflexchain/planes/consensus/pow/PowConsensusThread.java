package pt.unl.fct.di.hyflexchain.planes.consensus.pow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.unl.fct.di.hyflexchain.planes.application.ti.InvalidTransactionException;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.TransactionManagement;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.txpool.TxPool;

public class PowConsensusThread implements Runnable {

	protected static final Logger LOG = LoggerFactory.getLogger(PowConsensusThread.class.getSimpleName());

	private final int nTxsInBlock;

	private final PowConsensus consensus;

	/**
	 * @param consensus
	 * @param nTxsInBlock
	 */
	public PowConsensusThread(PowConsensus consensus, int nTxsInBlock) {
		this.consensus = consensus;
		this.nTxsInBlock = nTxsInBlock;
	}

	@Override
	public void run() {

		TransactionManagement txManag = TransactionManagement.getInstance();
		TxPool txPool;
		try {
			txPool = txManag.getTxPool(ConsensusMechanism.PoW);
		} catch (InvalidTransactionException e) {
			e.printStackTrace();
			throw new Error(e.getMessage(), e);
		}

		try {
			while (true) {
				var txs = txPool.waitForMinPendingTxs(this.nTxsInBlock);
				var block = this.consensus.createBlock(txs);
				this.consensus.orderBlock(block);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
