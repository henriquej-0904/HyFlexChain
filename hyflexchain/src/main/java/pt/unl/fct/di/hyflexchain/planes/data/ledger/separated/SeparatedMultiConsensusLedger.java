package pt.unl.fct.di.hyflexchain.planes.data.ledger.separated;

import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.tuweni.bytes.Bytes;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.BlockFilter;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.bft.BftCommittee;
import pt.unl.fct.di.hyflexchain.planes.data.DataPlane;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockState;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.LedgerState;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.inmemory.InMemoryLedger;
import pt.unl.fct.di.hyflexchain.util.ResetInterface;
import pt.unl.fct.di.hyflexchain.util.config.MultiLedgerConfig;
import pt.unl.fct.di.hyflexchain.util.crypto.HashedObject;

/**
 * A Multi Consensus Ledger, i.e. there is a separate ledger
 * for each different consensus mechanism. So, the state of
 * those multiple ledger instances is never merged.
 */
public class SeparatedMultiConsensusLedger implements DataPlane, ResetInterface
{
	protected static SeparatedMultiConsensusLedger instance;

	public static SeparatedMultiConsensusLedger getInstance()
	{
		if (instance != null)
			return instance;

		synchronized(SeparatedMultiConsensusLedger.class)
		{
			if (instance != null)
				return instance;

			instance = new SeparatedMultiConsensusLedger(
				MultiLedgerConfig.getInstance(), HyFlexChainBlock.GENESIS_BLOCK);

			return instance;
		}
	}

	protected final MultiLedgerConfig configs;

	protected EnumMap<ConsensusMechanism, ConsensusSpecificLedger> ledgerByConsensus;

	protected BiConsumer<HashedObject<HyFlexChainBlock>, BftCommittee> uponNewBftCommittee;

	/**
	 * Create a new instance of the ledger
	 * @param ledgerConfig The configuration of the ledger.
	 */
	protected SeparatedMultiConsensusLedger(MultiLedgerConfig ledgerConfig,
		EnumMap<ConsensusMechanism, HashedObject<HyFlexChainBlock>> genesisBlocks)
	{
		this.configs = ledgerConfig;
		this.ledgerByConsensus = initLedgers(genesisBlocks);
		this.uponNewBftCommittee = null;
	}

	/**
	 * Init all specific ledgers.
	 * @return A map with all specific ledgers.
	 */
	protected EnumMap<ConsensusMechanism, ConsensusSpecificLedger>
		initLedgers(EnumMap<ConsensusMechanism, HashedObject<HyFlexChainBlock>> genesisBlocks)
	{
		EnumMap<ConsensusMechanism, ConsensusSpecificLedger> res = new EnumMap<>(ConsensusMechanism.class);

		for (ConsensusMechanism c : ConsensusMechanism.values()) {
			res.put(c,
				new InMemoryLedger(c)
					.init(this.configs.getLedgerConfig(c), genesisBlocks.get(c))
			);
		}

		return res;
	}

	protected ConsensusSpecificLedger getLedgerInstance(ConsensusMechanism consensus)
	{
		return this.ledgerByConsensus.get(consensus);
	}

	@Override
	public void reset() {
		this.ledgerByConsensus = initLedgers(HyFlexChainBlock.GENESIS_BLOCK);
		this.uponNewBftCommittee = null;
	}

	@Override
	public MultiLedgerConfig getLedgerConfig() {
		return this.configs;
	}

	@Override
	public void writeOrderedBlock(HashedObject<HyFlexChainBlock> block, ConsensusMechanism consensusType) {
		getLedgerInstance(consensusType).writeOrderedBlock(block);
	}

	@Override
	public Optional<HyFlexChainBlock> getBlock(Bytes id, ConsensusMechanism consensus) {
		return getLedgerInstance(consensus).getBlock(id);
	}

	@Override
	public Optional<BlockState> getBlockState(Bytes id, ConsensusMechanism consensus) {
		return getLedgerInstance(consensus).getBlockState(id);
	}

	@Override
	public List<HashedObject<HyFlexChainBlock>> getBlocks(BlockFilter filter, ConsensusMechanism consensus) {
		return getLedgerInstance(consensus).getBlocks(filter);
	}

	@Override
	public HashedObject<HyFlexChainBlock> getLastBlock(ConsensusMechanism consensus) {
		return getLedgerInstance(consensus).getLastBlock();
	}

	@Override
	public EnumMap<ConsensusMechanism, LedgerState> getLedger() {
		return Stream.of(ConsensusMechanism.values())
			.collect(
				Collectors.toMap(
					(c) -> c,
					this::getLedger,
					(c1, c2) -> c1,
					() -> new EnumMap<ConsensusMechanism, LedgerState>(ConsensusMechanism.class)
				)
			);
	}

	@Override
	public LedgerState getLedger(ConsensusMechanism consensus) {
		return getLedgerInstance(consensus).getLedger();
	}

	@Override
	public void uponNewBlock(Consumer<HashedObject<HyFlexChainBlock>> action, ConsensusMechanism consensus) {
		getLedgerInstance(consensus).uponNewBlock(action);
	}

	@Override
	public int blockchainSize(ConsensusMechanism consensus) {
		return getLedgerInstance(consensus).blockchainSize();
	}

	@Override
	public void writeOrderedBftCommitteeBlock(HashedObject<HyFlexChainBlock> block, BftCommittee committee) {
		// Technically the block should be written to the storage
		if (this.uponNewBftCommittee != null)
			this.uponNewBftCommittee.accept(block, committee);
	}

	@Override
	public void uponNewBftCommitteeBlock(BiConsumer<HashedObject<HyFlexChainBlock>, BftCommittee> action) {
		if (this.uponNewBftCommittee == null)
			this.uponNewBftCommittee = action;
		else
			this.uponNewBftCommittee = this.uponNewBftCommittee.andThen(action);
	}
	
}
