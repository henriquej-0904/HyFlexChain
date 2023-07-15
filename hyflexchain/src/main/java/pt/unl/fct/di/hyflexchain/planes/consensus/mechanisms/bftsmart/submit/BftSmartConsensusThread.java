package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.submit;

import java.net.URI;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.unl.fct.di.hyflexchain.api.rest.impl.client.HyFlexChainHttpClient;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.bft.BftCommittee;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockBody;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.planes.network.Host;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.TransactionManagement;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.txpool.TxPool;
import pt.unl.fct.di.hyflexchain.util.config.LedgerConfig;

public class BftSmartConsensusThread implements Runnable {

	protected static final Logger LOG = LoggerFactory.getLogger(BftSmartConsensusThread.class);

	private final Random rand;

	private final HyFlexChainHttpClient hyflexchainClient;

	private final Address selfAddress;

	private final int nTxsInBlock;
	private final long blockCreateTime;

	private final ConsensusInterface consensus;

	private final Supplier<Pair<BftCommittee, Map<Address, Host>>> activeCommittee;

	private BftCommittee committee;

	private Host[] committeeMembers;

	private boolean isInCommittee;

	/**
	 * @param consensus
	 * @param nTxsInBlock
	 */
	public BftSmartConsensusThread(ConsensusInterface consensus, LedgerConfig config,
		Supplier<Pair<BftCommittee, Map<Address, Host>>> activeCommittee) {
		this.rand = new Random(System.currentTimeMillis());
		this.hyflexchainClient = new HyFlexChainHttpClient();
		this.selfAddress = config.getMultiLedgerConfig().getSelfAddress();
		this.consensus = consensus;
		this.nTxsInBlock = config.getNumTxsInBlock();
		this.blockCreateTime = config.getCreateBlockTime();
		this.activeCommittee = activeCommittee;
	}

	protected void updateCommittee()
	{
		var currentCommitteePair = this.activeCommittee.get();
		var currentCommittee = currentCommitteePair.getLeft();
		if (currentCommittee != this.committee)
		{
			this.committee = currentCommittee;
			this.committeeMembers = currentCommitteePair.getRight().values().toArray(Host[]::new);
			this.isInCommittee = currentCommittee.getCommitteeAddresses().contains(this.selfAddress);
		}
	}

	protected Host getRandomCommitteeMember()
	{
		return this.committeeMembers [
			this.rand.nextInt(this.committeeMembers.length)
		];
	}

	@Override
	public void run() {

		TransactionManagement txManag = TransactionManagement.getInstance();
		TxPool txPool = txManag.getTxPool(consensus.getConsensus());

		try {
			while (true) {

				updateCommittee();

				if (this.isInCommittee)
				{
					// submit transactions to ordering

					var txs = txPool.waitForMinPendingTxs(this.nTxsInBlock, this.blockCreateTime);

					LOG.info("BFT-SMART: Order block of transactions");

					this.consensus.orderTxs(BlockBody.from(txs));
				}
				else
				{
					URI redirect = getRandomCommitteeMember().httpEndpoint();

					// redirect transactions to committee
					var txs = txPool.getAllPendingTxs();

					LOG.info("BFT-SMART: Redirecting {} txs to {}", txs.size(), redirect);

					for (var tx : txs) {
						this.hyflexchainClient.sendTransactionAsync(redirect, tx,
							new RedirectTransaction(tx, txPool));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
