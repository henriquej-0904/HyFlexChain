package pt.unl.fct.di.hyflexchain.planes.execution;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.execution.contracts.InvalidSmartContractException;
import pt.unl.fct.di.hyflexchain.planes.execution.contracts.TransactionParamsContract.TransactionParamsContractResult;

/**
 * Represents the Execution Plane.
 * In the context of HyFlexChain, this plane will be used
 * when verifying transactions with the purpose of
 * identifying which {@link ConsensusMechanism} is responsible
 * for ordering the transaction, with some optional parameters.
 */
public interface ExecutionPlane {
    
    static ExecutionPlane getInstance()
    {
        return ExecutionPlaneImpl.getInstance();
    }

    /**
     * Execute the smart contract present in the transaction
     * and get the {@link TransactionParamsContractResult} result.
     * The provided smart contract will be deployed on a temporary
     * EVM account and executed on a stateless environment.
     * @param tx The transaction which contains the smart contract to be executed.
     * @return {@link TransactionParamsContractResult}
     * @exception InvalidSmartContractException if an error occurred in the deploy or execution phase.
     */
    TransactionParamsContractResult callGetTransactionParams(HyFlexChainTransaction tx) throws InvalidSmartContractException;

}
