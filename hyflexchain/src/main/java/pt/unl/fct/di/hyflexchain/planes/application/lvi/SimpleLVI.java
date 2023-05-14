package pt.unl.fct.di.hyflexchain.planes.application.lvi;

import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.consensus.LedgerViewConsensusInterface;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.consensus.PowLVI;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.data.DataPlane;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockState;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.LedgerState;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionState;

public class SimpleLVI implements LedgerViewInterface {

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
	public SimpleLVI() {
		this.lvis = new EnumMap<>(ConsensusMechanism.class);
		this.lvis.put(ConsensusMechanism.PoW, new PowLVI());
		
		this.data = DataPlane.getInstance();
	}

	@Override
	public Optional<HyFlexChainTransaction> getTransaction(String id, ConsensusMechanism consensus) {
		return getLVI(consensus).getTransaction(id);
	}

	@Override
	public TransactionState getTransactionState(String id, ConsensusMechanism consensus) {
		return getLVI(consensus).getTransactionState(id);
	}

	@Override
	public Optional<HyFlexChainBlock> getBlock(String id, ConsensusMechanism consensus) {
		return data.getBlock(id, consensus);
	}

	@Override
	public Optional<BlockState> getBlockState(String id, ConsensusMechanism consensus) {
		return data.getBlockState(id, consensus);
	}

	@Override
	public List<HyFlexChainBlock> getBlocks(BlockFilter filter, ConsensusMechanism consensus) {
		return data.getBlocks(filter, consensus);
	}

	@Override
	public String getLastBlockHash(ConsensusMechanism consensus) {
		return data.getLastBlock(consensus).header().getMetaHeader().getHash();
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
	public Committee getActiveCommittee(ConsensusMechanism consensus) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getActiveCommittee'");
	}

	@Override
	public List<Committee> getLedgerViewPreviousCommittees(int lastN, ConsensusMechanism consensus) {
		return data.getLedgerViewPreviousCommittees(lastN, consensus);
	}

	public LedgerViewConsensusInterface getLVI(ConsensusMechanism consensus)
	{
		var lvi = this.lvis.get(consensus);

		if (lvi == null)
			throw new UnsupportedOperationException("There is no LVI for:" + consensus);

		return lvi;
	}
	
}
