package pt.unl.fct.di.hyflexchain.planes.data.transaction.wrapper;

import org.apache.tuweni.bytes.Bytes;

import pt.unl.fct.di.hyflexchain.planes.application.ti.InvalidTransactionException;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.SerializedTx;

public interface TxWrapper {
    
    /**
     * Gets the transaction
     * @return The transaction
     * @throws InvalidTransactionException if an error occurred while
     * deserializing the transaction
     */
    HyFlexChainTransaction tx() throws InvalidTransactionException;

    /**
     * Serialize the transaction
     * @return The serialized transaction
     * @throws InvalidTransactionException if an error occurred while
     * serializing the transaction
     */
    SerializedTx serializedTx() throws InvalidTransactionException;

    /**
     * Get the hash of the wrapped transaction
     * @return The hash of the wrapped transaction
     * @throws InvalidTransactionException
     */
    default Bytes txHash() throws InvalidTransactionException
    {
        return serializedTx().hash();
    }

    /**
     * Create a wrapper from the specified transaction
     * @param tx The transaction to wrap
     * @return The transaction wrapper
     */
    static TxWrapper from(HyFlexChainTransaction tx)
    {
        return new DecodedTxWrapper(tx);
    }

    /**
     * Create a wrapper from the specified serialized
     * transaction.
     * @param serializedTx The serialized transaction to wrap
     * @return The transaction wrapper
     */
    static TxWrapper from(byte[] serializedTx)
    {
        return new SerializedTxWrapper(serializedTx);
    }

}
