package pt.unl.fct.di.hyflexchain.planes.execution;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.tuweni.bytes.Bytes;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.planes.execution.ExecutionPlaneImpl.DeployedContract;
import pt.unl.fct.di.hyflexchain.planes.execution.contracts.InvalidSmartContractException;

public class ExecutionPlaneUpdaterImpl implements ExecutionPlaneUpdater {

    final ExecutionPlaneImpl executionPlane;

    final Map<Address, DeployedContract> installed;
    
    final Set<Address> revoked;

    
    /**
     * @param executionPlane
     */
    public ExecutionPlaneUpdaterImpl(ExecutionPlaneImpl executionPlane) {
        this.executionPlane = executionPlane;
        this.installed = new HashMap<>(10);
        this.revoked = new HashSet<>(10);
    }

    @Override
    public void commit() {
        executionPlane.update(this);
    }

    @Override
    public void deploySmartContract(Address account, Address contractAddress, Bytes contractCode)
            throws InvalidSmartContractException {
        deploySmartContract0(account, contractAddress, contractCode);
    }

    @Override
    public void revokeSmartContract(Address account, Address contractAddress) throws InvalidSmartContractException {
        revokeSmartContract0(account, contractAddress);
    }

    void deploySmartContract0(Address account,Address contractAddress, Bytes contractCode)
        throws InvalidSmartContractException
    {
        if (!isAddressValidForNewContract0(contractAddress))
                throw new InvalidSmartContractException("Invalid address for smart contract: already used");

        // create execution context to verify the smart contract
        executionPlane.createExecutionContext(contractCode);

        // install smart contract
        installed.put(contractAddress, new DeployedContract(account, contractCode));
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

    boolean isSmartContractDeployed0(Address contractAddress)
    {
        boolean res = installed.containsKey(contractAddress);

        if (res)
            return true;

        return executionPlane.isSmartContractDeployed(contractAddress);
    }

    boolean isSmartContractDeployed0(Address account,Address contractAddress)
    {
        var deployedContract = installed.get(contractAddress);
        boolean res = deployedContract == null ? false :
            deployedContract.account().equals(account);

        if (res)
            return true;
        
        return executionPlane.isSmartContractDeployed(account, contractAddress);
    }

    boolean isSmartContractRevoked0(Address contractAddress)
    {
        return revoked.contains(contractAddress) ||
            executionPlane.isSmartContractRevoked(contractAddress);
    }

    boolean isAddressValidForNewContract0(Address contractAddress)
    {
        return !isSmartContractDeployed0(contractAddress) && !isSmartContractRevoked0(contractAddress);
    }
    
}
