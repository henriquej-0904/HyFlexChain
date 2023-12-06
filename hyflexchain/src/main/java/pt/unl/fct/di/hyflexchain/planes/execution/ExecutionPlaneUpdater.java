package pt.unl.fct.di.hyflexchain.planes.execution;

import org.apache.tuweni.bytes.Bytes;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.planes.execution.contracts.InvalidSmartContractException;

public interface ExecutionPlaneUpdater {
    
    /**
     * Commit the changes made to this updater to the
     * underlying execution plane.
     */
    void commit();

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
}
