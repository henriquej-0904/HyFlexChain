package pt.unl.fct.di.hyflexchain.planes.execution.contracts;

import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.evm.Code;
import org.hyperledger.besu.evm.worldstate.WorldUpdater;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;

import pt.unl.fct.di.hyflexchain.evm.contract.SmartContract;
import pt.unl.fct.di.hyflexchain.evm.executor.EvmExecutor;
import pt.unl.fct.di.hyflexchain.planes.consensus.params.ConsensusParams;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.UTXO;

public class ConsensusParamsContract extends SmartContract {

    public static final String FUNC_GETCONSENSUSPARAMS = "getConsensusParams";

    public ConsensusParamsContract(Address contractAddress, Code code) {
        super(contractAddress, code);
    }
    
    public static ConsensusParamsContract deploy(EvmExecutor evm, Address sender, Bytes codeBytes, WorldUpdater worldUpdater) throws InvalidSmartContractException
    {
        try {
            var result = evm.deploySmartContract(sender, codeBytes, worldUpdater);
            return new ConsensusParamsContract(result.getLeft(), result.getRight());
        } catch (RuntimeException e) {
            throw new InvalidSmartContractException(e.getMessage(), e);
        }
        
    }

    /**
     * Call the getConsensusParams of this smart contract
     * @param evm The executor
     * @param sender the evm sender address
     * @param worldUpdater the world updater
     * @param tx the transaction context for the call
     * @return consensus parameters.
     * @throws InvalidSmartContractException
     */
    @SuppressWarnings("rawtypes")
    public ConsensusParams callGetConsensusParams(final EvmExecutor evm, final Address sender,
        final WorldUpdater worldUpdater, HyFlexChainTransaction tx) throws InvalidSmartContractException
    {
        long totalValue = Stream.of(tx.getOutputTxs())
        .filter((utxo) -> !utxo.recipient().equals(tx.getSender()))
        .mapToLong(UTXO::value)
        .sum();

        final Function function = new Function(
                FUNC_GETCONSENSUSPARAMS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(tx.getVersion()), 
                new org.web3j.abi.datatypes.Utf8String(tx.getHash()), 
                new org.web3j.abi.datatypes.Utf8String(tx.getSender().address()), 
                new org.web3j.abi.datatypes.Utf8String(tx.getSignatureType()), 
                new org.web3j.abi.datatypes.Utf8String(tx.getSignature()), 
                new org.web3j.abi.datatypes.generated.Int64(tx.getNonce()), 
                new org.web3j.abi.datatypes.generated.Int32(tx.getInputTxs().length), 
                new org.web3j.abi.datatypes.generated.Int32(tx.getOutputTxs().length), 
                new org.web3j.abi.datatypes.generated.Int64(totalValue), 
                new org.web3j.abi.datatypes.generated.Int64(System.currentTimeMillis() / 1000)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));

        try {
            var res = executeConstantCallSingleValueReturn(evm, sender, function, String.class, worldUpdater);
            return ConsensusParams.parse(res);
        } catch (RuntimeException e) {
            throw new InvalidSmartContractException(e.getMessage(), e);
        }
    }

}
