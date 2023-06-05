package pt.unl.fct.di.hyflexchain.planes.execution;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.params.ConsensusParams;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.execution.contracts.InvalidSmartContractException;

/**
 * Represents the Execution Plane.
 * In the context of HyFlexChain, this plane will be used
 * when verifying transactions with the purpose of
 * identifying which {@link ConsensusMechanism} (with some configurations) is responsible
 * for ordering the transaction. 
 */
public interface ExecutionPlane {
    
    static ExecutionPlane getInstance()
    {
        return ExecutionPlaneImpl.getInstance();
    }

    /**
     * Execute the smart contract present in the transaction
     * and get the {@link ConsensusParams} result.
     * The provided smart contract will be deployed on a temporary
     * EVM account and executed on a stateless environment.
     * @param tx The transaction which contains the smart contract to be executed.
     * @return {@link ConsensusParams}
     * @exception InvalidSmartContractException if an error occurred in the deploy or execution phase.
     */
    ConsensusParams callGetConsensusParams(HyFlexChainTransaction tx) throws InvalidSmartContractException;

}
