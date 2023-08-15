package pt.unl.fct.di.hyflexchain.planes.execution;

import org.apache.tuweni.bytes.Bytes;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
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
     * Execute the smart contract code provided by the specified transaction
     * and get the {@link TransactionParamsContractResult} result.
     * The provided smart contract will be executed on a stateless environment.
     * @param code The transaction that contains the smart contract code or
     * a reference to a previously installed one.
     * @return {@link TransactionParamsContractResult}
     * @exception InvalidSmartContractException if an error occurred in execution phase.
     */
    TransactionParamsContractResult executeSmartContract(HyFlexChainTransaction tx) throws InvalidSmartContractException;

    /**
     * Deploys a smart contract to the specified contract address with the given code.
     * The deployed contract is associated with the specified account and can only be
     * revoked by that account.
     * @param account The account that deployed this smart contract
     * @param contractAddress The address which the deployed contract will be accessible
     * @param contractCode The code of the contract
     * @throws InvalidSmartContractException if an error occurred in the deploy
     * (ex. a contract already exists with the specified address).
     */
    void deploySmartContract(Address account, Address contractAddress, Bytes contractCode) throws InvalidSmartContractException;

    /**
     * Revokes a smart contract. This operation can only be performed
     * by the account that deployed the contract through the method
     * {@link #deploySmartContract(Address, Address, Bytes)}.
     * @param account The account that deployed this smart contract
     * @param contractAddress The address of the contract to revoke
     * @throws InvalidSmartContractException if an error occurred in the revoke process
     * (ex. the contract does not exist, the account is now its owner or it has already been revoked).
     */
    void revokeSmartContract(Address account, Address contractAddress) throws InvalidSmartContractException;

    /**
     * Checks if the specified address points to a deployed and valid smart contract
     * @param contractAddress The address to check
     * @return true if it is deployed and valid
     */
    boolean isSmartContractDeployed(Address contractAddress);

    /**
     * Checks if the specified contract address points to a deployed and valid smart contract
     * and that the account is its owner.
     * @param account The account that deployed this smart contract
     * @param contractAddress The address to check
     * @return true if it is deployed and valid
     */
    boolean isSmartContractDeployed(Address account, Address contractAddress);

    /**
     * Checks if the specified address is a valid address for a
     * new smart contract.
     * @param contractAddress The address to check
     * @return true if is valid.
     */
    boolean isAddressValidForNewContract(Address contractAddress);
}
