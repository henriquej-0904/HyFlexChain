package pt.unl.fct.di.hyflexchain.planes.consensus.pow;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.TransactionManagement;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.txpool.TxPool;

public class PowConsensusThread implements Runnable {

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
		TxPool txPool = txManag.getTxPool(ConsensusMechanism.PoW);

		while (true) {
			// wait for min number of txs in tx pool
			while (txPool.size() < this.nTxsInBlock) {
				Thread.onSpinWait();
			}

			var txs = txPool.getTxs(nTxsInBlock);
			if (txs.size() < this.nTxsInBlock)
				continue;

			var block = consensus.createBlock(txs);
			this.consensus.orderBlock(block);
		}
	}
}
