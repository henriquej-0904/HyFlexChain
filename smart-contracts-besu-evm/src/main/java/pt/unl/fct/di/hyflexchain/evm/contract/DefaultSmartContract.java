package pt.unl.fct.di.hyflexchain.evm.contract;

import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.evm.Code;

public class DefaultSmartContract extends SmartContract {

    private final Address contractAddress;
    private final Code code;

    /**
     * @param contractAddress
     * @param code
     */
    public DefaultSmartContract(Address contractAddress, Code code) {
        this.contractAddress = contractAddress;
        this.code = code;
    }

    @Override
    public Address getContractAddress() {
        return this.contractAddress;
    }

    @Override
    public Code getCode() {
        return this.code;
    }
    
}
