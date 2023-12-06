package pt.unl.fct.di.hyflexchain.planes.application.scmi;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionType;
import pt.unl.fct.di.hyflexchain.planes.application.ti.InvalidTransactionException;
import pt.unl.fct.di.hyflexchain.planes.application.ti.TransactionInterface;

/**
 * The Smart Contract Management Interface (SCMI)
 * is responsible for processing requests to install or revoke
 * smart contracts. <p>
 * 
 * This interface is for private use, only accessible for the owner
 * of the executing HyFlexChain node, as the requests originated
 * from this interface are signed with the node's private key. <p>
 * 
 * The installation and revoke requests are represented as
 * normal transactions of the respective {@link TransactionType}:
 * CONTRACT_CREATE and CONTRACT_REVOKE.
 * Therefore, these requests can also be performed through the
 * public {@link TransactionInterface}, which allows to create or revoke
 * smart contracts from any verified user.
 */
public interface SmartContractManagementInterface {
	
	static SmartContractManagementInterface getInstance()
	{
		return SCMIImpl.getInstance();
	}

	/**
	 * Create a new smart contract with the provided code.
	 * The owner of the newly created smart contract is
	 * the node that process this request.
	 * @param contractCode The code of the smart contract.
	 * @return The address of the newly created smart contract.
	 */
	String installSmartContract(byte[] contractCode) throws InvalidTransactionException;

	/**
	 * Revoke the specified smart contract.
	 * This operation only permits to revoke smart contracts
	 * owned by the node executing this operation.
	 * @param contractAddress The address of the smart contract to revoke
	 * @return The address of the revoked smart contract.
	 */
	String revokeSmartContract(Address contractAddress) throws InvalidTransactionException;

}
