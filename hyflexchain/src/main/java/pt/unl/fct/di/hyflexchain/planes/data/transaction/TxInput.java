package pt.unl.fct.di.hyflexchain.planes.data.transaction;

/**
 * Represents a Transaction Input, aka a reference to a UTXO.
 * 
 * @param txHash Pointer to the transaction containing the UTXO to be spent
 * @param outputIndex The index number of the UTXO to be spent; first one is 0
 */
public record TxInput(
	String txHash, int outputIndex
)
{
}
