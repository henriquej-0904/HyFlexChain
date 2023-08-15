package pt.unl.fct.di.hyflexchain.planes.data.ledger.separated;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.tuweni.bytes.Bytes;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.BlockFilter;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockState;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.LedgerState;
import pt.unl.fct.di.hyflexchain.util.config.LedgerConfig;
import pt.unl.fct.di.hyflexchain.util.crypto.HashedObject;

/**
 * An interface for a consensus specific ledger implementation.
 */
public interface ConsensusSpecificLedger
{
	/**
	 * Initialize the ledger with the provided configurations.
	 * @param config The configuration of the ledger.
	 * @param genesisBlock The genesis block
	 * @return The initialized object
	 */
	ConsensusSpecificLedger init(LedgerConfig config, HashedObject<HyFlexChainBlock> genesisBlock);

	/**
	 * Get the consensus mechanism of this ledger implementation.
	 * @return The consensus mechanism
	 */
	ConsensusMechanism getConsensusMechanism();

	/**
	 * Write an ordered block to the Ledger.
	 * @param block The ordered block
	 */
	void writeOrderedBlock(HashedObject<HyFlexChainBlock> block);

	/**
	 * Get a transaction.
	 * @param id The id of the transaction
	 * @return The transaction.
	 */
	// Optional<HyFlexChainTransaction> getTransaction(TransactionId id);

	/**
	 * Get the state of a transaction.
	 * @param id The id of the transaction
	 * @return The state of the transaction.
	 */
	// Optional<TransactionState> getTransactionState(TransactionId id);

	/**
	 * Get all transactions where the specified account is the origin.
	 * @param address The public key of the account
	 * @return All transactions where the specified account is the origin.
	 */
	// List<HyFlexChainTransaction> getTransactionsByOriginAccount(String address);

	/**
	 * Get all transactions where the specified account is the destination.
	 * @param address The address of the account
	 * @return All transactions where the specified account is the destination.
	 */
	// List<HyFlexChainTransaction> getTransactionsByDestAccount(String address);

	/**
	 * Get all transactions according to the specified filter
	 * where the specified account is the origin.
	 * @param address The address of the account
	 * @param filter The filter
	 * @return All filtered transactions where the specified account is the origin.
	 */
	// List<HyFlexChainTransaction> getTransactionsByOriginAccount(String address, TransactionFilter filter);

	/**
	 * Get all transactions according to the specified filter
	 * where the specified account is the destination.
	 * @param address The address of the account
	 * @param filter The filter
	 * @return All filtered transactions where the specified account is the destination.
	 */
	// List<HyFlexChainTransaction> getTransactionsByDestAccount(String address, TransactionFilter filter);

	/**
	 * Get all transactions according to the specified filter.
	 * @param filter The filter
	 * @return All filtered transactions.
	 */
	// List<HyFlexChainTransaction> getTransactions(TransactionFilter filter);

	//#endregion


	//#region Blocks

	/**
	 * Get a block.
	 * @param id The id of the block
	 * @return The block.
	 */
	Optional<HyFlexChainBlock> getBlock(Bytes id);

	HashedObject<HyFlexChainBlock> getLastBlock();

	/**
	 * Get the state of a block.
	 * @param id The id of the block
	 * @return The state of the block.
	 */
	Optional<BlockState> getBlockState(Bytes id);

	/**
	 * Get all blocks according to the specified filter.
	 * @param filter The filter
	 * @return All filtered blocks.
	 */
	List<HashedObject<HyFlexChainBlock>> getBlocks(BlockFilter filter);

	/**
	 * Get the number of blocks in the blockchain.
	 * @return the number of blocks in the blockchain.
	 */
	int blockchainSize();

	//#endregion

	/**
	 * Get the full Ledger State.
	 * @return The full Ledger State.
	 */
	LedgerState getLedger();

	/**
	 * Get a ledger view of the UTXO set.
	 * @return UTXO set
	 */
	// UTXOset getLedgerViewUTXOset();

	/**
	 * Get a ledger view of previous committees.
	 * 
	 * @param lastN The previous N committees
	 * @return Previous Committees.
	 */
	// List<Committee> getLedgerViewPreviousCommittees(int lastN);

	/**
	 * Call the specified action upon a new block is appended
	 * to the chain.
	 * @param action The action to call
	 */
	public void uponNewBlock(Consumer<HashedObject<HyFlexChainBlock>> action);

	//////////////////////////////////////////// COMMITTEES ////////////////////////////////////////////

	/**
	 * Write an ordered block with a committee to the Ledger.
	 * @param block The ordered block
	 * @param committee The committee in the block.
	 */
	// void writeOrderedCommitteeBlock(HashedObject<HyFlexChainBlock> block, Committee committee);

	/**
	 * Call the specified action upon a new block with a committee is appended
	 * to the chain.
	 * @param action The action to call
	 */
	// public void uponNewCommitteeBlock(Consumer<HashedObject<HyFlexChainBlock>> action);
}
