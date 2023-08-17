package pt.unl.fct.di.hyflexchain.planes.application.scmi;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.unl.fct.di.hyflexchain.planes.application.ti.InvalidTransactionException;
import pt.unl.fct.di.hyflexchain.planes.application.ti.TransactionInterface;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.SmartContract;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.wrapper.TxWrapper;
import pt.unl.fct.di.hyflexchain.util.config.MultiLedgerConfig;
import pt.unl.fct.di.hyflexchain.util.crypto.SignatureAlgorithm;

public class SCMIImpl implements SmartContractManagementInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(SCMIImpl.class);

    private static SCMIImpl instance;

    public static SmartContractManagementInterface getInstance()
    {
        if (instance != null)
			return instance;

		synchronized(SCMIImpl.class)
		{
			if (instance != null)
				return instance;

			instance = new SCMIImpl(MultiLedgerConfig.getInstance());
			return instance;
		}
    }

    private final MultiLedgerConfig config;

    private final SecureRandom rand;

    /**
     * @param config
     */
    private SCMIImpl(MultiLedgerConfig config) {
        this.config = config;
        try {
            this.rand = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new Error(e.getMessage(), e);
        }
    }

    @Override
    public String installSmartContract(byte[] contractCode) throws InvalidTransactionException {
        var tx = HyFlexChainTransaction.createContract(
            config.getSelfAddress(),
            null, null, rand.nextLong(),
            SmartContract.create(contractCode));

        try {
            tx.sign(config.getSelfKeyPair().getPrivate(), SignatureAlgorithm.SHA256withECDSA);
        } catch (InvalidKeyException | SignatureException e) {
            LOGGER.error(e.getMessage(), e);
            throw new Error(e.getMessage(), e);
        }

        return TransactionInterface.getInstance().sendTransactionAndWait(TxWrapper.from(tx));
    }

    @Override
    public String revokeSmartContract(Address contractAddress) throws InvalidTransactionException {
        var tx = HyFlexChainTransaction.revokeContract(
            config.getSelfAddress(),
            null, null, rand.nextLong(),
            SmartContract.reference(contractAddress));

        try {
            tx.sign(config.getSelfKeyPair().getPrivate(), SignatureAlgorithm.SHA256withECDSA);
        } catch (InvalidKeyException | SignatureException e) {
            LOGGER.error(e.getMessage(), e);
            throw new Error(e.getMessage(), e);
        }

        return TransactionInterface.getInstance().sendTransactionAndWait(TxWrapper.from(tx));
    }
    
}
