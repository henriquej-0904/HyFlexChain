package pt.unl.fct.di.hyflexchain.planes.data.transaction.wrapper;

import java.io.IOException;

import pt.unl.fct.di.hyflexchain.planes.application.ti.InvalidTransactionException;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.SerializedTx;

final class DecodedTxWrapper implements TxWrapper {

    private final HyFlexChainTransaction tx;

    private SerializedTx serializedTx;

    /**
     * @param tx
     */
    DecodedTxWrapper(HyFlexChainTransaction tx) {
        this.tx = tx;
    }

    @Override
    public HyFlexChainTransaction tx() throws InvalidTransactionException {
        return tx;
    }

    @Override
    public SerializedTx serializedTx() throws InvalidTransactionException {
        if (serializedTx != null)
            return serializedTx;

        try {
            return this.serializedTx = SerializedTx.from(tx);
        } catch (IOException e) {
            var ite = new InvalidTransactionException(e.getMessage(), e);
            ite.printStackTrace();
            throw ite;
        }
    }
    
}
