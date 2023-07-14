package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.bft.BftCommittee;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockBody;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.TransactionManagement;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.txpool.TxPool;
import pt.unl.fct.di.hyflexchain.util.config.LedgerConfig;

public class BftSmartConsensusThread implements Runnable {

	protected static final Logger LOG = LoggerFactory.getLogger(BftSmartConsensusThread.class);

	private final int nTxsInBlock;
	private final long blockCreateTime;

	private final ConsensusInterface consensus;

	private final Supplier<BftCommittee> activeCommittee;

	private BftCommittee previousCommittee;

	private boolean isInCommittee;

	/**
	 * @param consensus
	 * @param nTxsInBlock
	 */
	public BftSmartConsensusThread(ConsensusInterface consensus, LedgerConfig config,
		Supplier<BftCommittee> activeCommittee) {
		this.consensus = consensus;
		this.nTxsInBlock = config.getNumTxsInBlock();
		this.blockCreateTime = config.getCreateBlockTime();
		this.activeCommittee = activeCommittee;
	}

	@Override
	public void run() {

		TransactionManagement txManag = TransactionManagement.getInstance();
		TxPool txPool = txManag.getTxPool(consensus.getConsensus());

		try {
			while (true) {
				var txs = txPool.waitForMinPendingTxs(this.nTxsInBlock, this.blockCreateTime);

				LOG.info("BFT-SMART: Order block of transactions");

				this.consensus.orderTxs(BlockBody.from(txs));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
