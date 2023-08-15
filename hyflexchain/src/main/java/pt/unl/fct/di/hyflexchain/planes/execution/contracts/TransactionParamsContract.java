package pt.unl.fct.di.hyflexchain.planes.execution.contracts;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.evm.Code;
import org.hyperledger.besu.evm.worldstate.WorldUpdater;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.generated.Int64;

import com.fasterxml.jackson.core.JsonProcessingException;

import pt.unl.fct.di.hyflexchain.evm.contract.SmartContract;
import pt.unl.fct.di.hyflexchain.evm.executor.EvmExecutor;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.UTXO;
import pt.unl.fct.di.hyflexchain.util.Utils;

/**
 * A {@link SmartContract} used by HyFlexChain to decide which
 * {@link ConsensusMechanism} it will be used to order a transaction.
 * Additionally, other options can be decided through the execution of
 * this contract, ex. Batch metric, type of signature, etc.
 */
public class TransactionParamsContract extends SmartContract {

    public static final String FUNC_GETTRANSACTIONPARAMS = "getTransactionParams";

    public TransactionParamsContract(Address contractAddress, Code code) {
        super(contractAddress, code);
    }

    public static TransactionParamsContract deploy(EvmExecutor evm, Address sender, Bytes codeBytes,
            WorldUpdater worldUpdater) throws InvalidSmartContractException {
        try {
            var result = evm.deploySmartContract(sender, codeBytes, worldUpdater);
            return new TransactionParamsContract(result.getLeft(), result.getRight());
        } catch (RuntimeException e) {
            throw new InvalidSmartContractException(e.getMessage(), e);
        }

    }

    /**
     * Call the getTransactionParams of this smart contract and get
     * the result.
     * 
     * @param evm          The executor
     * @param sender       the evm sender address
     * @param worldUpdater the world updater
     * @param tx           the transaction context for the call
     * @return {@link TransactionParamsContractResult}
     * @throws InvalidSmartContractException
     */
    @SuppressWarnings("rawtypes")
    public TransactionParamsContractResult callgetTransactionParams(final EvmExecutor evm, final Address sender,
            final WorldUpdater worldUpdater, HyFlexChainTransaction tx) throws InvalidSmartContractException {
        final List<Int64> outputTxsValue = Stream.of(tx.getOutputTxs())
                .mapToLong(UTXO::value).<Int64>mapToObj(Int64::new).toList();

        final List<Utf8String> outputTxsAddress = Stream.of(tx.getOutputTxs())
                .map(UTXO::recipient)
                .map(pt.unl.fct.di.hyflexchain.planes.data.transaction.Address::toHexString)
                .map(Utf8String::new).toList();

        final Function function = new Function(
                FUNC_GETTRANSACTIONPARAMS,
                Arrays.<Type>asList(
                        new Utf8String(tx.getVersion()),
                        new Utf8String(tx.getSender().toHexString()),
                        new DynamicArray<Int64>(Int64.class, outputTxsValue),
                        new DynamicArray<Utf8String>(Utf8String.class, outputTxsAddress),
                        new Int64(System.currentTimeMillis() / 1000)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
                }));

        try {
            var res = executeConstantCallSingleValueReturn(evm, sender, function, String.class, worldUpdater);
            return TransactionParamsContractResult.parse(res);
        } catch (RuntimeException e) {
            throw new InvalidSmartContractException(e.getMessage(), e);
        }
    }

    // /**
    //  * Call the getTransactionParams of this smart contract
    //  * 
    //  * @param evm          The executor
    //  * @param sender       the evm sender address
    //  * @param worldUpdater the world updater
    //  * @param tx           the transaction context for the call
    //  * @return consensus parameters.
    //  * @throws InvalidSmartContractException
    //  */
    // @SuppressWarnings("rawtypes")
    // public TransactionParams callgetTransactionParams(final EvmExecutor evm, final Address sender,
    //         final WorldUpdater worldUpdater, HyFlexChainTransaction tx) throws InvalidSmartContractException {
    //     long totalValue = Stream.of(tx.getOutputTxs())
    //             .filter((utxo) -> !utxo.recipient().equals(tx.getSender()))
    //             .mapToLong(UTXO::value)
    //             .sum();

    //     final Function function = new Function(
    //             FUNC_GETTRANSACTIONPARAMS,
    //             Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(tx.getVersion()),
    //                     new org.web3j.abi.datatypes.Utf8String(tx.getHash()),
    //                     new org.web3j.abi.datatypes.Utf8String(tx.getSender().address()),
    //                     new org.web3j.abi.datatypes.Utf8String(tx.getSignatureType()),
    //                     new org.web3j.abi.datatypes.Utf8String(tx.getSignature()),
    //                     new org.web3j.abi.datatypes.generated.Int64(tx.getNonce()),
    //                     new org.web3j.abi.datatypes.generated.Int32(tx.getInputTxs().length),
    //                     new org.web3j.abi.datatypes.generated.Int32(tx.getOutputTxs().length),
    //                     new org.web3j.abi.datatypes.generated.Int64(totalValue),
    //                     new org.web3j.abi.datatypes.generated.Int64(System.currentTimeMillis() / 1000)),
    //             Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
    //             }));

    //     try {
    //         var res = executeConstantCallSingleValueReturn(evm, sender, function, String.class, worldUpdater);
    //         return TransactionParams.parse(res);
    //     } catch (RuntimeException e) {
    //         throw new InvalidSmartContractException(e.getMessage(), e);
    //     }
    // }

    /**
     * The decided parameters for a transaction.
     */
    public static class TransactionParamsContractResult {

        private ConsensusMechanism consensus;

        private OptionalInt batchMetric;

        private Optional<String> signatureType;

        /**
         * 
         */
        public TransactionParamsContractResult() {
        }

        /**
         * Parse the value as a ConsensusParams object.
         * 
         * @param value
         * @return TransactionParams
         */
        public static TransactionParamsContractResult parse(String value) {
            try {
                return Utils.json.readValue(value, TransactionParamsContractResult.class);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException(e.getMessage() + "\nSmart Contract Result:" +
                value, e);
            }
        }

        /**
         * @return the consensus
         */
        public ConsensusMechanism getConsensus() {
            return consensus;
        }

        /**
         * @param consensus the consensus to set
         */
        public void setConsensus(ConsensusMechanism consensus) {
            this.consensus = consensus;
        }

        /**
         * @return the batchMetric
         */
        public OptionalInt getBatchMetric() {
            return batchMetric;
        }

        /**
         * @param batchMetric the batchMetric to set
         */
        public void setBatchMetric(OptionalInt batchMetric) {
            this.batchMetric = batchMetric;
        }

        /**
         * @return the signatureType
         */
        public Optional<String> getSignatureType() {
            return signatureType;
        }

        /**
         * @param signatureType the signatureType to set
         */
        public void setSignatureType(Optional<String> signatureType) {
            if (signatureType.isPresent() && 
                signatureType.get().equals("default"))
                this.signatureType = Optional.empty();
            else
                this.signatureType = signatureType;
        }
    }

}
