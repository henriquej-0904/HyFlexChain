package pt.unl.fct.di.hyflexchain.evm.contract;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.evm.Code;
import org.hyperledger.besu.evm.worldstate.WorldUpdater;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;

import pt.unl.fct.di.hyflexchain.evm.executor.EvmExecutor;

/**
 * A smart contract that runs on the evm.
 */
public class SmartContract {

    private final Address contractAddress;
    private final Code code;

    /**
     * @param contractAddress
     * @param code
     */
    public SmartContract(Address contractAddress, Code code) {
        this.contractAddress = contractAddress;
        this.code = code;
    }

    /**
     * @return the contractAddress
     */
    public Address getContractAddress() {
        return this.contractAddress;
    }

    /**
     * @return the code
     */
    public Code getCode() {
        return this.code;
    }

    /**
     * Execute constant function call - i.e. a call that does not change state of
     * the contract
     * 
     * @param evm
     * @param sender
     * @param function     to call
     * @param worldUpdater
     * @return {@link List} of values returned by function call
     */
    @SuppressWarnings("rawtypes")
    private List<Type> executeConstantCall(final EvmExecutor evm, final Address sender, final Function function,
            final WorldUpdater worldUpdater) {
        final String encodedFunction = FunctionEncoder.encode(function);
        final Bytes inputData = Bytes.fromHexString(encodedFunction);

        final Bytes result = evm.execute(sender, getCode(), inputData, Wei.ZERO, getContractAddress(), worldUpdater);

        return FunctionReturnDecoder.decode(result.toHexString(), function.getOutputParameters());
    }

    
    /**
     * Execute constant function call - i.e. a call that does not change state of
     * the contract.
     * @param <T> The Solidity return type
     * @param evm The evm executor
     * @param sender The caller of the contract
     * @param function to call
     * @param worldUpdater the world updater
     * @return The result
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T extends Type> T executeConstantCallSingleValueReturn(final EvmExecutor evm, final Address sender,
            final Function function, final WorldUpdater worldUpdater) {
        List<Type> values = executeConstantCall(evm, sender, function, worldUpdater);
        if (!values.isEmpty()) {
            return (T) values.get(0);
        } else {
            return null;
        }
    }

    /**
     * Execute constant function call - i.e. a call that does not change state of
     * the contract.
     * @param <T> The Solidity return type
     * @param <R> The equivalent java return type
     * @param evm The evm executor
     * @param sender The caller of the contract
     * @param function to call
     * @param returnType The java return type
     * @param worldUpdater the world updater
     * @return The result of the specified java type
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T extends Type, R> R executeConstantCallSingleValueReturn(
            final EvmExecutor evm, final Address sender, final Function function, final Class<R> returnType,
            final WorldUpdater worldUpdater) {
        T result = executeConstantCallSingleValueReturn(evm, sender, function, worldUpdater);
        if (result == null) {
            throw new ContractCallException("Empty value (0x) returned from contract");
        }

        Object value = result.getValue();
        if (returnType.isAssignableFrom(value.getClass())) {
            return (R) value;
        } else if (result.getClass().equals(org.web3j.abi.datatypes.Address.class) && returnType.equals(String.class)) {
            return (R) result.toString(); // cast isn't necessary
        } else {
            throw new ContractCallException(
                    "Unable to convert response: "
                            + value
                            + " to expected type: "
                            + returnType.getSimpleName());
        }
    }

    /**
     * Execute constant function call - i.e. a call that does not change state of
     * the contract.
     * @param evm The evm executor
     * @param sender The caller of the contract
     * @param function to call
     * @param worldUpdater the world updater
     * @return {@link List} of values returned by function call
     */
    @SuppressWarnings({"rawtypes"})
    public List<Type> executeConstantCallMultipleValueReturn(final EvmExecutor evm, final Address sender,
            final Function function, final WorldUpdater worldUpdater) {
        return executeConstantCall(evm, sender, function, worldUpdater);
    }
}
