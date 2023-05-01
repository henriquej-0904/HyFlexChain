package pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.blockchain;

/**
 * A way to locate a transaction on the ledger.
 * 
 * @param blockHash The hash of the block containing this transaction
 * @param txHash The hash of the transaction
 */
public record TxFinder(String blockHash, String txHash) {
	
}
