package pt.unl.fct.di.hyflexchain.planes.data.ledger.separated;

import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.BlockFilter;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.views.HistoryPreviousCommittees;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.views.UTXOset;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.data.DataPlane;
import pt.unl.fct.di.hyflexchain.planes.data.TransactionFilter;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockState;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.LedgerState;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.inmemory.InMemoryLedger;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionId;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionState;
import pt.unl.fct.di.hyflexchain.util.config.MultiLedgerConfig;

/**
 * A Multi Consensus Ledger, i.e. there is a separate ledger
 * for each different consensus mechanism. So, the state of
 * those multiple ledger instances is never merged.
 */
public class SeparatedMultiConsensusLedger implements DataPlane
{
	protected final MultiLedgerConfig configs;

	protected final EnumMap<ConsensusMechanism, ConsensusSpecificLedger> ledgerByConsensus;

	/**
	 * Create a new instance of the ledger
	 * @param ledgerConfig The configuration of the ledger.
	 */
	protected SeparatedMultiConsensusLedger(MultiLedgerConfig ledgerConfig)
	{
		this.configs = ledgerConfig;
		this.ledgerByConsensus = initLedgers();
	}

	/**
	 * Init all specific ledgers.
	 * @return A map with all specific ledgers.
	 */
	protected EnumMap<ConsensusMechanism, ConsensusSpecificLedger> initLedgers()
	{
		var ledgerType = MultiLedgerConfig.GENERAL_CONFIG
			.LEDGER_DB_TYPE.getLedgerDbTypeValue();

		EnumMap<ConsensusMechanism, ConsensusSpecificLedger> res = new EnumMap<>(ConsensusMechanism.class);

		switch (ledgerType) {
			case IN_MEMORY:
				for (ConsensusMechanism c : ConsensusMechanism.values()) {
					res.put(c,
						new InMemoryLedger(c)
							.init(this.configs.getLedgerConfig(c))
					);
				}
				break;
		}

		return res;
	}

	protected ConsensusSpecificLedger getLedgerInstance(ConsensusMechanism consensus)
	{
		return this.ledgerByConsensus.get(consensus);
	}

	@Override
	public MultiLedgerConfig getLedgerConfig() {
		return this.configs;
	}

	@Override
	public void writeOrderedBlock(HyFlexChainBlock block, ConsensusMechanism consensusType) {
		getLedgerInstance(consensusType).writeOrderedBlock(block);
	}

	@Override
	public Optional<HyFlexChainTransaction> getTransaction(TransactionId id, ConsensusMechanism consensus) {
		return getLedgerInstance(consensus).getTransaction(id);
	}

	@Override
	public Optional<TransactionState> getTransactionState(TransactionId id, ConsensusMechanism consensus) {
		return getLedgerInstance(consensus).getTransactionState(id);
	}

	@Override
	public List<HyFlexChainTransaction> getTransactionsByOriginAccount(String originPubKey,
			ConsensusMechanism consensus)
	{
		return getLedgerInstance(consensus).getTransactionsByOriginAccount(originPubKey);
	}

	@Override
	public List<HyFlexChainTransaction> getTransactionsByDestAccount(String destPubKey, ConsensusMechanism consensus) {
		return getLedgerInstance(consensus).getTransactionsByDestAccount(destPubKey);
	}

	@Override
	public List<HyFlexChainTransaction> getTransactionsByOriginAccount(String originPubKey, TransactionFilter filter,
			ConsensusMechanism consensus)
	{
		return getLedgerInstance(consensus).getTransactionsByOriginAccount(originPubKey, filter);
	}

	@Override
	public List<HyFlexChainTransaction> getTransactionsByDestAccount(String destPubKey, TransactionFilter filter,
			ConsensusMechanism consensus)
	{
		return getLedgerInstance(consensus).getTransactionsByDestAccount(destPubKey, filter);
	}

	@Override
	public List<HyFlexChainTransaction> getTransactions(TransactionFilter filter, ConsensusMechanism consensus) {
		return getLedgerInstance(consensus).getTransactions(filter);
	}

	@Override
	public Optional<HyFlexChainBlock> getBlock(String id, ConsensusMechanism consensus) {
		return getLedgerInstance(consensus).getBlock(id);
	}

	@Override
	public Optional<BlockState> getBlockState(String id, ConsensusMechanism consensus) {
		return getLedgerInstance(consensus).getBlockState(id);
	}

	@Override
	public List<HyFlexChainBlock> getBlocks(BlockFilter filter, ConsensusMechanism consensus) {
		return getLedgerInstance(consensus).getBlocks(filter);
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
	public UTXOset getLedgerViewUTXOset(ConsensusMechanism consensus) {
		return getLedgerInstance(consensus).getLedgerViewUTXOset();
	}

	@Override
	public Committee getActiveCommittee(ConsensusMechanism consensus) {
		return getLedgerInstance(consensus).getActiveCommittee();
	}

	@Override
	public HistoryPreviousCommittees getLedgerViewPreviousCommittees(int lastN, ConsensusMechanism consensus) {
		return getLedgerInstance(consensus).getLedgerViewPreviousCommittees(lastN);
	}
	
}
