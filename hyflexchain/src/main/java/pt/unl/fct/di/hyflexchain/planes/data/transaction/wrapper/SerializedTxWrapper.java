package pt.unl.fct.di.hyflexchain.planes.data.transaction.wrapper;

import java.io.IOException;

import pt.unl.fct.di.hyflexchain.planes.application.ti.InvalidTransactionException;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.SerializedTx;

final class SerializedTxWrapper implements TxWrapper {

    private final byte[] serializedTxBytes;

    private HyFlexChainTransaction tx;

    private SerializedTx serializedTx;

    /**
     * @param serializedTxBytes
     */
    SerializedTxWrapper(byte[] serializedTxBytes) {
        this.serializedTxBytes = serializedTxBytes;
    }

    @Override
    public HyFlexChainTransaction tx() throws InvalidTransactionException {
        if (tx != null)
            return tx;

        try {
            return tx = serializedTx().deserialize();
        } catch (IOException e) {
            var ite = new InvalidTransactionException(e.getMessage(), e);
            ite.printStackTrace();
            throw ite;
        }
    }

    @Override
    public SerializedTx serializedTx() throws InvalidTransactionException {
        if (serializedTx != null)
            return serializedTx;

        return serializedTx = SerializedTx.from(serializedTxBytes);
    }
    
}
