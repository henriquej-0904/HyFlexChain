package pt.unl.fct.di.hyflexchain.planes.execution;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import org.apache.commons.lang3.function.FailableRunnable;
import org.apache.commons.lang3.function.FailableSupplier;
import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.evm.account.EvmAccount;
import org.hyperledger.besu.evm.fluent.SimpleWorld;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.hyperledger.besu.evm.tracing.StandardJsonTracer;
import org.hyperledger.besu.evm.worldstate.WorldUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.unl.fct.di.hyflexchain.evm.executor.EvmExecutor;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.execution.contracts.TransactionParamsContract;
import pt.unl.fct.di.hyflexchain.planes.execution.contracts.TransactionParamsContract.TransactionParamsContractResult;
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

    private final Map<Address, DeployedContract> installed;
    
    private final Set<Address> revoked;

    private final ReadWriteLock lock;

    /**
     * Create a new instance of the execution plane.
     */
    public ExecutionPlaneImpl() {
        this.evm = EvmExecutor.london(EvmConfiguration.DEFAULT);

        if (LOG.isTraceEnabled())
            this.evm.tracer(new StandardJsonTracer(System.out, false, true, true));

        this.updater = new SimpleWorld();

        this.installed = new HashMap<>(100);
        this.revoked = new HashSet<>(100);
        this.lock = new ReentrantReadWriteLock();
    }

    private EvmAccount createDefaultAccount(WorldUpdater updater)
    {
        return updater.createAccount(org.hyperledger.besu.datatypes.Address.wrap(Bytes.random(20)), 0, Wei.MAX_WEI);
    }

    private WorldUpdater getTmpUpdater()
    {
        return this.updater.updater();
    }

    void update(ExecutionPlaneUpdaterImpl updater)
    {
        processOperationSynchronizedWrite(() -> {
            installed.putAll(updater.installed);
            revoked.addAll(updater.revoked);
        });
    }

    @Override
    public ExecutionPlaneUpdater getUpdater() {
        return new ExecutionPlaneUpdaterImpl(this);
    }

    @Override
    public TransactionParamsContractResult executeSmartContract(HyFlexChainTransaction tx) throws InvalidSmartContractException
    {
        FailableSupplier<TransactionParamsContractResult, InvalidSmartContractException> op =
            () -> executeSmartContract0(tx);
        return getFailableValueSynchronizedRead(op);
    }

    TransactionParamsContractResult executeSmartContract0(HyFlexChainTransaction tx) throws InvalidSmartContractException
    {
        ExecutionContext context;
        if (tx.getSmartContract().isAddressProvided())
            context = createExecutionContext(tx.getSmartContract().id());
        else if (tx.getSmartContract().isCodeProvided())
            context = createExecutionContext(Bytes.wrap(tx.getSmartContract().code()));
        else
            throw new InvalidSmartContractException("Cannot execute smart contract: nor contract address or code was provided.");

        return context.execute(tx);
    }

    ExecutionContext createExecutionContext(Address contractAddress) throws InvalidSmartContractException
    {
        var deployedContract = installed.get(contractAddress);

        if (deployedContract == null)
            throw new InvalidSmartContractException("Cannot execute referenced smart contract: smart contract does not exist.");

        return deployedContract.context();
    }

    ExecutionContext createExecutionContext(Bytes contractCode) throws InvalidSmartContractException
    {
        final WorldUpdater updater = getTmpUpdater();
        final EvmAccount sender = createDefaultAccount(updater);

        var contract = TransactionParamsContract.deploy(this.evm, sender.getAddress(), contractCode, updater);
        return new ExecutionContext(contract, sender, updater);
    }

    @Override
    public void deploySmartContract(Address account,Address contractAddress, Bytes contractCode)
            throws InvalidSmartContractException
    {
        FailableRunnable<InvalidSmartContractException> op =
            () -> deploySmartContract0(account, contractAddress, contractCode);
        processFailableOperationSynchronizedWrite(op);
    }

    @Override
    public void simulateDeploySmartContract(Address account, Address contractAddress, Bytes contractCode)
            throws InvalidSmartContractException
    {
        FailableRunnable<InvalidSmartContractException> op =
            () ->
        {
            if (!isAddressValidForNewContract0(contractAddress))
                throw new InvalidSmartContractException("Invalid address for smart contract: already used");

            // create execution context to verify the smart contract
            createExecutionContext(contractCode);
        };
        processFailableOperationSynchronizedRead(op);
    }

    void deploySmartContract0(Address account,Address contractAddress, Bytes contractCode)
        throws InvalidSmartContractException
    {
        if (!isAddressValidForNewContract0(contractAddress))
                throw new InvalidSmartContractException("Invalid address for smart contract: already used");

        // create execution context to verify the smart contract
        var context = createExecutionContext(contractCode);

        // install smart contract
        installed.put(contractAddress, new DeployedContract(account, context));
    }

    @Override
    public void revokeSmartContract(Address account,Address contractAddress)
            throws InvalidSmartContractException {
        FailableRunnable<InvalidSmartContractException> op =
            () -> revokeSmartContract0(account, contractAddress);
        processFailableOperationSynchronizedWrite(op);
    }

    void revokeSmartContract0(Address account,Address contractAddress)
            throws InvalidSmartContractException
    {
        if (!isSmartContractDeployed0(account, contractAddress))
            throw new InvalidSmartContractException("Cannot revoke contract: contract does not exist or specified account is not the owner.");

        if (isSmartContractRevoked0(contractAddress))
            throw new InvalidSmartContractException("Cannot revoke contract: already revoked.");

        installed.remove(contractAddress);
        revoked.add(contractAddress);
    }

    @Override
    public boolean isSmartContractDeployed(Address contractAddress) {
        return getValueSynchronizedRead(() -> isSmartContractDeployed0(contractAddress));
    }

    @Override
    public boolean isSmartContractDeployed(Address account,Address contractAddress) {
        return getValueSynchronizedRead(() -> isSmartContractDeployed0(account, contractAddress));
    }

    @Override
    public boolean isSmartContractRevoked(Address contractAddress)
    {
        return getValueSynchronizedRead(() -> isSmartContractRevoked0(contractAddress));
    }

    @Override
    public boolean isAddressValidForNewContract(Address contractAddress) {
        return getValueSynchronizedRead(() -> isAddressValidForNewContract0(contractAddress));
    }

    boolean isSmartContractDeployed0(Address contractAddress)
    {
        return installed.containsKey(contractAddress);
    }

    boolean isSmartContractDeployed0(Address account,Address contractAddress)
    {
        var deployedContract = installed.get(contractAddress);
        return deployedContract == null ? false :
            deployedContract.account().equals(account);
    }

    boolean isSmartContractRevoked0(Address contractAddress)
    {
        return revoked.contains(contractAddress);
    }

    boolean isAddressValidForNewContract0(Address contractAddress)
    {
        return !isSmartContractDeployed0(contractAddress) && !isSmartContractRevoked0(contractAddress);
    }

    <T> T getValueSynchronizedRead(Supplier<T> get)
    {
        try {
            lock.readLock().lock();
            return get.get();
        } finally
        {
            lock.readLock().unlock();
        }
    }

    <T, E extends Throwable> T getFailableValueSynchronizedRead(FailableSupplier<T, E> operation) throws E
    {
        try {
            lock.readLock().lock();
            return operation.get();
        } finally
        {
            lock.readLock().unlock();
        }
    }

    <E extends Throwable> void processFailableOperationSynchronizedRead(FailableRunnable<E> operation) throws E
    {
        try {
            lock.readLock().lock();
            operation.run();
        } finally
        {
            lock.readLock().unlock();
        }
    }

    void processOperationSynchronizedWrite(Runnable operation)
    {
        try {
            lock.writeLock().lock();
            operation.run();
        } finally
        {
            lock.writeLock().unlock();
        }
    }

    <E extends Throwable> void processFailableOperationSynchronizedWrite(FailableRunnable<E> operation) throws E
    {
        try {
            lock.writeLock().lock();
            operation.run();
        } finally
        {
            lock.writeLock().unlock();
        }
    }

    /**
     * DeployedContract
     */
    record DeployedContract(Address account, ExecutionContext context) {
    }

    /**
     * The execution context of a deployed smart contract.
     */
    class ExecutionContext
    {
        final TransactionParamsContract contract;

        final EvmAccount contractDeployAccount;

        final WorldUpdater state;

        /**
         * Create a new Execution context.
         * @param contract The deployed smart contract
         * @param contractDeployAccount The EVM account that deployed this contract
         * @param state The state required for the execution of the contract.
         */
        ExecutionContext(TransactionParamsContract contract, EvmAccount contractDeployAccount,
                WorldUpdater state) {
            this.contract = contract;
            this.contractDeployAccount = contractDeployAccount;
            this.state = state;
        }

        /**
         * Execute the smart contract on the specified transaction
         * to get the consensus dynamic policies.
         * @param tx
         * @return {@link TransactionParamsContractResult}
         * @throws InvalidSmartContractException if an error occurred in the execution of the contract.
         */
        TransactionParamsContractResult execute(HyFlexChainTransaction tx) throws InvalidSmartContractException
        {
            return contract.callgetTransactionParams(evm, contractDeployAccount.getAddress(), state, tx);
        }

    }
    
}
