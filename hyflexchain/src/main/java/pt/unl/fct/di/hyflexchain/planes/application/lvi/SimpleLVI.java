package pt.unl.fct.di.hyflexchain.planes.application.lvi;

import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.consensus.BftSmartLVI;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.consensus.LedgerViewConsensusInterface;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.consensus.PowLVI;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.CommitteeId;
import pt.unl.fct.di.hyflexchain.planes.data.DataPlane;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockState;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.LedgerState;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionState;
import pt.unl.fct.di.hyflexchain.util.ResetInterface;
import pt.unl.fct.di.hyflexchain.util.config.MultiLedgerConfig;
import pt.unl.fct.di.hyflexchain.util.crypto.HashedObject;

public class SimpleLVI implements LedgerViewInterface, ResetInterface {

	private static SimpleLVI instance;

	public static SimpleLVI getInstance()
	{
		if (instance != null)
			return instance;

		synchronized(SimpleLVI.class)
		{
			if (instance != null)
				return instance;

			instance = new SimpleLVI();
			return instance;
		}
	}

	protected final EnumMap<ConsensusMechanism, LedgerViewConsensusInterface> lvis;

	protected final DataPlane data;

	/**
	 * 
	 */
	private SimpleLVI() {
		
		this.lvis = MultiLedgerConfig.getInstance().getActiveConsensusMechanisms()
			.stream().collect(Collectors.toMap(
				UnaryOperator.identity(),
				this::initLVI,
				(x, y) -> x,
				() -> new EnumMap<>(ConsensusMechanism.class)
				)
		);
		
		this.data = DataPlane.getInstance();
	}

	protected LedgerViewConsensusInterface initLVI(ConsensusMechanism consensus)
	{
		switch (consensus) {
			case PoW:
				return new PowLVI();
			case BFT_SMaRt:
				return new BftSmartLVI();
		}

		return null;
	}

	@Override
	public void reset() {
		for (var lvi : this.lvis.values()) {
			((ResetInterface) lvi).reset();
		}
	}

	@Override
	public Optional<HyFlexChainTransaction> getTransaction(Bytes id, ConsensusMechanism consensus) {
		return getLVI(consensus).getTransaction(id);
	}

	@Override
	public TransactionState getTransactionState(Bytes id, ConsensusMechanism consensus) {
		return getLVI(consensus).getTransactionState(id);
	}

	@Override
	public Optional<HyFlexChainBlock> getBlock(Bytes id, ConsensusMechanism consensus) {
		return data.getBlock(id, consensus);
	}

	@Override
	public Optional<BlockState> getBlockState(Bytes id, ConsensusMechanism consensus) {
		return data.getBlockState(id, consensus);
	}

	@Override
	public List<HashedObject<HyFlexChainBlock>> getBlocks(BlockFilter filter, ConsensusMechanism consensus) {
		return data.getBlocks(filter, consensus);
	}

	@Override
	public Bytes getLastBlockHash(ConsensusMechanism consensus) {
		return data.getLastBlock(consensus).hash();
	}

	@Override
	public long getBlockchainSize(ConsensusMechanism consensus) {
		return data.blockchainSize(consensus);
	}

	@Override
	public EnumMap<ConsensusMechanism, LedgerState> getLedger() {
		return data.getLedger();
	}

	@Override
	public LedgerState getLedger(ConsensusMechanism consensus) {
		return data.getLedger(consensus);
	}

	@Override
	public Optional<Entry<CommitteeId, ? extends Committee>> getActiveCommittee(ConsensusMechanism consensus) {
		return getLVI(consensus).getActiveCommittee();
	}

	@Override
	public List<? extends Committee> getLedgerViewPreviousCommittees(int lastN, ConsensusMechanism consensus) {
		return getLVI(consensus).getLedgerViewPreviousCommittees(lastN);
	}

	@Override
	public Optional<Entry<CommitteeId, ? extends Committee>> getNextCommittee(ConsensusMechanism consensus) {
		return getLVI(consensus).getNextCommittee();
	}

	@Override
	public LedgerViewConsensusInterface getLVI(ConsensusMechanism consensus)
	{
		var lvi = this.lvis.get(consensus);

		if (lvi == null)
			throw new UnsupportedOperationException("There is no LVI for:" + consensus);

		return lvi;
	}
	
}
