package pt.unl.fct.di.hyflexchain.planes.data.ledger.separated;

/**
 * A way to locate a list of transactions in the same block on the ledger.
 * 
 * @param blockHash The hash of the block containing the transactions
 * @param txHashes The hashes of the transactions
 */
public record TxFinderList(String blockHash, java.util.List<String> txHashes) {
	
}
