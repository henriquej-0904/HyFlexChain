package pt.unl.fct.di.hyflexchain.planes.txmanagement;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Collection;
import java.util.EnumMap;

import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.unl.fct.di.hyflexchain.planes.application.ti.InvalidTransactionException;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.InvalidAddressException;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.SerializedTx;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionType;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.wrapper.TxWrapper;
import pt.unl.fct.di.hyflexchain.planes.execution.ExecutionPlane;
import pt.unl.fct.di.hyflexchain.planes.execution.ExecutionPlaneUpdater;
import pt.unl.fct.di.hyflexchain.planes.execution.contracts.InvalidSmartContractException;
import pt.unl.fct.di.hyflexchain.planes.execution.contracts.TransactionParamsContract.TransactionParamsContractResult;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.txpool.TxPool;
import pt.unl.fct.di.hyflexchain.util.ResetInterface;
import pt.unl.fct.di.hyflexchain.util.config.MultiLedgerConfig;

/**
 * An implementation of the TransactionManagement Interface
 * in which corresponds to the V2_0 version of SystemVersion,
 * i.e., there are 2 consensus and a smart contract is executed
 * to obtain consensus parameters for the ordering of a specific transaction.
 */
public class TransactionManagementV2_0 implements TransactionManagement, ResetInterface
{
	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionManagementV2_0.class);

	protected final EnumMap<ConsensusMechanism, TxPool> txPools;

	/**
	 * @param txPool
	 */
	public TransactionManagementV2_0() {
		this.txPools = new EnumMap<>(ConsensusMechanism.class);

        MultiLedgerConfig.getInstance().getActiveConsensusMechanisms().stream()
            .forEach((c) -> this.txPools.put(c, new TxPool(c))
        );
	}

	@Override
	public void reset() {
		for (var txPool : this.txPools.values()) {
			((ResetInterface) txPool).reset();
		}
	}

	@Override
	public TxPool getTxPool(ConsensusMechanism consensus) {
        TxPool pool = this.txPools.get(consensus);
        if (pool == null)
		    throw new Error("Consensus mechanism not supported: " + consensus.getConsensus());

        return pool;
	}

	/**
	 * Verifies if a transaction is valid, the cryptographic signature is correct
	 * and other aspects.
	 * @param tx The transaction to verify
	 * @throws InvalidTransactionException if the transaction was not verified.
	 */
    @Override
	public void verifyTx(HyFlexChainTransaction tx) throws InvalidTransactionException
	{
		try {
			if (! tx.verifySignature())
			{
				var msg = "Transaction invalid signature";
				LOGGER.info(msg);
				throw new InvalidTransactionException(msg);
			}

			// switch (tx.getTransactionType())
			// {
			// 	case COMMITTEE_ELECTION:
			// 		break;
			// 	case COMMITTEE_ROTATION:
			// 		break;
			// 	case CONTRACT_CREATE:
			// 		ExecutionPlane.getInstance()
			// 			.simulateDeploySmartContract(
			// 				tx.getSender(),
			// 				tx.getSmartContract().id(),
			// 				Bytes.wrap(tx.getSmartContract().code()));
			// 		break;
			// 	case CONTRACT_REVOKE:
			// 		if (!ExecutionPlane.getInstance()
			// 			.isSmartContractDeployed(tx.getSender(), tx.getSmartContract().id()))
			// 		{
			// 			throw new InvalidSmartContractException("There is no smart contract associated to the specified account.");
			// 		}
			// 		break;
			// 	case TRANSFER:
			// 		break;
			// 	default:
			// 		break;
				
			// }
				
		} catch (InvalidAddressException | InvalidKeyException |
				NoSuchAlgorithmException | SignatureException e) {
			LOGGER.info(e.getMessage());
			throw new InvalidTransactionException("Transaction verification failed: " + e.getMessage(), e);
		}
	}

    protected TransactionParamsContractResult callGetTransactionParams(HyFlexChainTransaction tx) throws InvalidTransactionException
    {
        try {
            return ExecutionPlane.getInstance().executeSmartContract(tx);
        } catch (InvalidSmartContractException e) {
            LOGGER.info(e.getMessage());
			throw new InvalidTransactionException(e.getMessage(), e);
        }
    }

	/**
	 * Check the result of the add pending tx operation.
	 * @param result The result of the add pending tx operation
	 * @throws InvalidTransactionException If the transaction was already submitted.
	 */
	protected void checkAddPendingTx(boolean result) throws InvalidTransactionException
	{
		if ( ! result )
		{
			var msg = "Invalid Transaction: Already submitted.";
			LOGGER.info(msg);
			throw new InvalidTransactionException(msg);
		}
	}

	@Override
	public String dispatchTransaction(TxWrapper tx) throws InvalidTransactionException
	{
		verifyTx(tx.tx());

		ConsensusMechanism c;
		if (tx.tx().getTransactionType() == TransactionType.TRANSFER)
		{
			c = callGetTransactionParams(tx.tx()).getConsensus();
		}
		else if (tx.tx().getTransactionType() == TransactionType.CONTRACT_CREATE ||
			tx.tx().getTransactionType() == TransactionType.CONTRACT_REVOKE)
		{
			c = ConsensusMechanism.PoW;
		}
		else
		{
			throw new InvalidTransactionException("Unsupported transaction type: " + tx.tx().getTransactionType());
		}

		checkAddPendingTx(getTxPool(c).addTxIfAbsent(tx.serializedTx()));	

		LOGGER.info("Submited {} tx [{}]: {}", tx.tx().getTransactionType(), c.getConsensus(), tx.txHash());
		return tx.txHash().toHexString();
	}

	@Override
	public String dispatchTransactionAndWait(TxWrapper tx) throws InvalidTransactionException
	{
		verifyTx(tx.tx());

		ConsensusMechanism c;
		if (tx.tx().getTransactionType() == TransactionType.TRANSFER)
		{
			c = callGetTransactionParams(tx.tx()).getConsensus();
		}
		else if (tx.tx().getTransactionType() == TransactionType.CONTRACT_CREATE ||
			tx.tx().getTransactionType() == TransactionType.CONTRACT_REVOKE)
		{
			c = ConsensusMechanism.PoW;
		}
		else
		{
			throw new InvalidTransactionException("Unsupported transaction type: " + tx.tx().getTransactionType());
		}

		try {
			checkAddPendingTx(getTxPool(c).addTxIfAbsentAndWait(tx.serializedTx(), LOGGER));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		LOGGER.info("Finalized tx: " + tx.txHash());
		return tx.txHash().toHexString();
	}

	protected SerializedTx serialize(HyFlexChainTransaction tx) throws InvalidTransactionException
	{
		try {
			return SerializedTx.from(tx);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new InvalidTransactionException(e.getMessage(), e);
		}
	}

	@Override
	public void executeTransactions(Collection<HyFlexChainTransaction> txs) throws InvalidTransactionException {
		
		ExecutionPlaneUpdater execUpdater = null;

		try {
			for (var tx : txs) {
				switch (tx.getTransactionType()) {
					case COMMITTEE_ELECTION:
						break;
					case COMMITTEE_ROTATION:
						break;
					case CONTRACT_CREATE:
						if (execUpdater == null)
							execUpdater = ExecutionPlane.getInstance().getUpdater();
						execUpdater
								.deploySmartContract(tx.getSender(),
										tx.getSmartContract().id(),
										Bytes.wrap(tx.getSmartContract().code()));
						break;
					case CONTRACT_REVOKE:
						if (execUpdater == null)
							execUpdater = ExecutionPlane.getInstance().getUpdater();
						execUpdater
								.revokeSmartContract(tx.getSender(),
										tx.getSmartContract().id());
						break;
					case TRANSFER:
						break;
					default:
						break;

				}
			}
		} catch (Exception e) {
			throw new InvalidTransactionException(e.getMessage(), e);
		}

		if (execUpdater != null)
			execUpdater.commit();
	}
	
}
