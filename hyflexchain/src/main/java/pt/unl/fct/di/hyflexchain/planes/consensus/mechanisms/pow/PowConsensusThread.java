package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.pow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockBody;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.TransactionManagement;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.txpool.TxPool;
import pt.unl.fct.di.hyflexchain.util.config.LedgerConfig;

public class PowConsensusThread implements Runnable {

	protected static final Logger LOG = LoggerFactory.getLogger(PowConsensusThread.class);

	private final int nTxsInBlock;
	private final long blockCreateTime;

	private final PowConsensus consensus;

	/**
	 * @param consensus
	 * @param nTxsInBlock
	 */
	public PowConsensusThread(PowConsensus consensus, LedgerConfig config) {
		this.consensus = consensus;
		this.nTxsInBlock = config.getNumTxsInBlock();
		this.blockCreateTime = config.getCreateBlockTime();
	}

	@Override
	public void run() {

		TransactionManagement txManag = TransactionManagement.getInstance();
		TxPool txPool = txManag.getTxPool(ConsensusMechanism.PoW);

		try {
			while (true) {
				var txs = txPool.waitForMinPendingTxs(this.nTxsInBlock, this.blockCreateTime);
				// var block = this.consensus.createBlock(txs);
				// this.consensus.orderBlock(block);
				this.consensus.orderTxs(BlockBody.from(txs));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
