package pt.unl.fct.di.hyflexchain.planes.data.transaction;

/**
 * The transaction Id is based on the address
 * of the sender and the tx hash.
 * 
 * @param senderAddress The address
 * of the sender of the transaction
 * 
 * @param txHash The hash of the transaction
 */
public record TransactionId(String senderAddress, String txHash) {}
