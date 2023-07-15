package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.submit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.InvocationCallback;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.txpool.TxPool;

public class RedirectTransaction implements InvocationCallback<String>
{
    protected static final Logger LOG = LoggerFactory.getLogger(RedirectTransaction.class);

    private final HyFlexChainTransaction tx;

    private final TxPool txPool;

    /**
     * @param tx
     */
    public RedirectTransaction(HyFlexChainTransaction tx, TxPool txPool) {
        this.tx = tx;
        this.txPool = txPool;
    }

    @Override
    public void completed(String response) {
        var hash = this.tx.getHash();
        boolean ok = response.equalsIgnoreCase(hash);
        this.txPool.removePendingTxAndNotify(hash, ok);

        LOG.info("Received reply from member of committee: {}", ok);
    }

    @Override
    public void failed(Throwable throwable) {
        if (throwable instanceof ProcessingException ex)
        {
            LOG.error("Cannot redirect transaction to committee: {}", ex.getMessage());
        }

        if (throwable instanceof WebApplicationException ex)
        {
            LOG.error("Received reply from member of committee with error code: {}",
                ex.getMessage());
        }

        var hash = this.tx.getHash();
        this.txPool.removePendingTxAndNotify(hash, false);
    }
    
}
