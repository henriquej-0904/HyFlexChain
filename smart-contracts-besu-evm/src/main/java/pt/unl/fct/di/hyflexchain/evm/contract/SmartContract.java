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

public abstract class SmartContract {

    /**
     * @return the contractAddress
     */
    public abstract Address getContractAddress();

    /**
     * @return the code
     */
    public abstract Code getCode();

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
    private List<Type> executeCall(final EvmExecutor evm, final Address sender, final Function function,
            final WorldUpdater worldUpdater) {
        final String encodedFunction = FunctionEncoder.encode(function);
        final Bytes inputData = Bytes.fromHexString(encodedFunction);

        final Bytes result = evm.execute(sender, getCode(), inputData, Wei.ZERO, getContractAddress(), worldUpdater);

        return FunctionReturnDecoder.decode(result.toHexString(), function.getOutputParameters());
    }

    @SuppressWarnings("unchecked")
    public <T extends Type> T executeCallSingleValueReturn(final EvmExecutor evm, final Address sender,
            final Function function, final WorldUpdater worldUpdater) {
        List<Type> values = executeCall(evm, sender, function, worldUpdater);
        if (!values.isEmpty()) {
            return (T) values.get(0);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Type, R> R executeCallSingleValueReturn(
            final EvmExecutor evm, final Address sender, final Function function, final Class<R> returnType,
            final WorldUpdater worldUpdater) {
        T result = executeCallSingleValueReturn(evm, sender, function, worldUpdater);
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

    public List<Type> executeCallMultipleValueReturn(final EvmExecutor evm, final Address sender,
            final Function function, final WorldUpdater worldUpdater) {
        return executeCall(evm, sender, function, worldUpdater);
    }
}
