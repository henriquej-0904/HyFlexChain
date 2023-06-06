package pt.unl.fct.di.hyflexchain.planes.execution;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.evm.account.EvmAccount;
import org.hyperledger.besu.evm.fluent.SimpleWorld;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.hyperledger.besu.evm.worldstate.WorldUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.unl.fct.di.hyflexchain.evm.executor.EvmExecutor;
import pt.unl.fct.di.hyflexchain.planes.consensus.params.ConsensusParams;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.execution.contracts.ConsensusParamsContract;
import pt.unl.fct.di.hyflexchain.planes.execution.contracts.InvalidSmartContractException;

/**
 * An implementation of the {@link ExecutionPlane}.
 */
public class ExecutionPlaneImpl implements ExecutionPlane
{
    protected static final Logger LOG = LoggerFactory.getLogger(ExecutionPlaneImpl.class);

    private static ExecutionPlane instance;

    public static ExecutionPlane getInstance()
    {
        if (instance != null)
		    return instance;

        synchronized(ExecutionPlaneImpl.class)
        {
            if (instance != null)
		        return instance;

            instance = new ExecutionPlaneImpl();
            return instance;
        }
    }


    private final EvmExecutor evm;

    private final WorldUpdater updater;

    /**
     * Create a new instance of the execution plane.
     */
    public ExecutionPlaneImpl() {
        this.evm = EvmExecutor.london(EvmConfiguration.DEFAULT);
        this.updater = new SimpleWorld();
    }

    private EvmAccount createDefaultAccount(WorldUpdater updater)
    {
        return updater.createAccount(Address.wrap(Bytes.random(20)), 0, Wei.MAX_WEI);
    }

    private WorldUpdater getTmpUpdater()
    {
        return this.updater.updater();
    }

    @Override
    public ConsensusParams callGetConsensusParams(HyFlexChainTransaction tx) throws InvalidSmartContractException
    {
        final WorldUpdater updater = getTmpUpdater();
        final EvmAccount sender = createDefaultAccount(updater);

        try {
            final ConsensusParamsContract contract =
            ConsensusParamsContract.deploy(this.evm, sender.getAddress(), Bytes.wrap(tx.getData()), updater);

            return contract.callGetConsensusParams(this.evm, sender.getAddress(), updater, tx);
        } catch (InvalidSmartContractException e) {
            LOG.info(e.getMessage());
            throw e;
        }
    }
    
}
