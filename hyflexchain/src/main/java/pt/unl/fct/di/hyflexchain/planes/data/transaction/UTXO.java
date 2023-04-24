package pt.unl.fct.di.hyflexchain.planes.data.transaction;

/**
 * Represents an Unspent Transaction Output
 * 
 * @param address The address of the receiving account
 * @param value The value to transfer
 */
public record UTXO(String address, long value)
{
}
