/*
 * Copyright contributors to Hyperledger Besu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package pt.unl.fct.di.hyflexchain.evm;

import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.evm.Code;
import org.hyperledger.besu.evm.EVM;
import org.hyperledger.besu.evm.MainnetEVMs;
import org.hyperledger.besu.evm.account.Account;
import org.hyperledger.besu.evm.code.CodeV0;
import org.hyperledger.besu.evm.contractvalidation.ContractValidationRule;
import org.hyperledger.besu.evm.contractvalidation.MaxCodeSizeRule;
import org.hyperledger.besu.evm.contractvalidation.PrefixCodeRule;
import org.hyperledger.besu.evm.frame.BlockValues;
import org.hyperledger.besu.evm.frame.MessageFrame;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.hyperledger.besu.evm.precompile.MainnetPrecompiledContracts;
import org.hyperledger.besu.evm.precompile.PrecompileContractRegistry;
import org.hyperledger.besu.evm.processor.ContractCreationProcessor;
import org.hyperledger.besu.evm.processor.MessageCallProcessor;
import org.hyperledger.besu.evm.tracing.OperationTracer;
import org.hyperledger.besu.evm.worldstate.WorldUpdater;
import org.hyperledger.besu.evm.fluent.SimpleWorld;
import org.hyperledger.besu.evm.fluent.SimpleBlockValues;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

/** The Evm executor. */
public class Evm {

  private final EVM evm;
  private PrecompileContractRegistry precompileContractRegistry;
  private boolean commitWorldState = false;
  private WorldUpdater worldUpdater = new SimpleWorld();
  private long gas = Long.MAX_VALUE;
  private Address receiver = Address.ZERO;
  private Address sender = Address.ZERO;
  private Wei gasPriceGWei = Wei.ZERO;
  private Bytes callData = Bytes.EMPTY;
  private Wei ethValue = Wei.ZERO;
  private Code code = CodeV0.EMPTY_CODE;
  private BlockValues blockValues = new SimpleBlockValues();
  private OperationTracer tracer = OperationTracer.NO_TRACING;
  private boolean requireDeposit = true;
  private List<ContractValidationRule> contractValidationRules =
      List.of(MaxCodeSizeRule.of(0x6000), PrefixCodeRule.of());
  private long initialNonce = 0;
  private Collection<Address> forceCommitAddresses = List.of(Address.fromHexString("0x03"));
  private Set<Address> accessListWarmAddresses = Set.of();
  private Multimap<Address, Bytes32> accessListWarmStorage = HashMultimap.create();
  private MessageCallProcessor messageCallProcessor = null;
  private ContractCreationProcessor contractCreationProcessor = null;

  private Evm(final EVM evm) {
    Objects.requireNonNull(evm, "evm must not be null");
    this.evm = evm;
  }

  /**
   * Instandiate Evm executor.
   *
   * @param evm the evm
   * @return the evm executor
   */
  public static Evm evm(final EVM evm) {
    return new Evm(evm);
  }

  /**
   * Instantiate Frontier evm executor.
   *
   * @param evmConfiguration the evm configuration
   * @return the evm executor
   */
  public static Evm frontier(final EvmConfiguration evmConfiguration) {
    final Evm executor = new Evm(MainnetEVMs.frontier(evmConfiguration));
    executor.precompileContractRegistry =
        MainnetPrecompiledContracts.frontier(executor.evm.getGasCalculator());
    executor.contractValidationRules = List.of();
    executor.requireDeposit = false;
    executor.forceCommitAddresses = List.of();
    return executor;
  }

  /**
   * Instantiate Homestead evm executor.
   *
   * @param evmConfiguration the evm configuration
   * @return the evm executor
   */
  public static Evm homestead(final EvmConfiguration evmConfiguration) {
    final Evm executor = new Evm(MainnetEVMs.homestead(evmConfiguration));
    executor.precompileContractRegistry =
        MainnetPrecompiledContracts.frontier(executor.evm.getGasCalculator());
    executor.contractValidationRules = List.of();
    executor.forceCommitAddresses = List.of();
    return executor;
  }

  /**
   * Instantiate Spurious dragon evm executor.
   *
   * @param evmConfiguration the evm configuration
   * @return the evm executor
   */
  public static Evm spuriousDragon(final EvmConfiguration evmConfiguration) {
    final Evm executor = new Evm(MainnetEVMs.spuriousDragon(evmConfiguration));
    executor.precompileContractRegistry =
        MainnetPrecompiledContracts.frontier(executor.evm.getGasCalculator());
    executor.contractValidationRules = List.of(MaxCodeSizeRule.of(0x6000));
    return executor;
  }

  /**
   * Instantiate Tangerine whistle evm executor.
   *
   * @param evmConfiguration the evm configuration
   * @return the evm executor
   */
  public static Evm tangerineWhistle(final EvmConfiguration evmConfiguration) {
    final Evm executor = new Evm(MainnetEVMs.tangerineWhistle(evmConfiguration));
    executor.precompileContractRegistry =
        MainnetPrecompiledContracts.frontier(executor.evm.getGasCalculator());
    executor.contractValidationRules = List.of(MaxCodeSizeRule.of(0x6000));
    return executor;
  }

  /**
   * Instantiate Byzantium evm executor.
   *
   * @param evmConfiguration the evm configuration
   * @return the evm executor
   */
  public static Evm byzantium(final EvmConfiguration evmConfiguration) {
    final Evm executor = new Evm(MainnetEVMs.byzantium(evmConfiguration));
    executor.precompileContractRegistry =
        MainnetPrecompiledContracts.byzantium(executor.evm.getGasCalculator());
    executor.contractValidationRules = List.of(MaxCodeSizeRule.of(0x6000));
    return executor;
  }

  /**
   * Instantiate Constantinople evm executor.
   *
   * @param evmConfiguration the evm configuration
   * @return the evm executor
   */
  public static Evm constantinople(final EvmConfiguration evmConfiguration) {
    final Evm executor = new Evm(MainnetEVMs.constantinople(evmConfiguration));
    executor.precompileContractRegistry =
        MainnetPrecompiledContracts.byzantium(executor.evm.getGasCalculator());
    executor.contractValidationRules = List.of(MaxCodeSizeRule.of(0x6000));
    return executor;
  }

  /**
   * Instantiate Petersburg evm executor.
   *
   * @param evmConfiguration the evm configuration
   * @return the evm executor
   */
  public static Evm petersburg(final EvmConfiguration evmConfiguration) {
    final Evm executor = new Evm(MainnetEVMs.petersburg(evmConfiguration));
    executor.precompileContractRegistry =
        MainnetPrecompiledContracts.byzantium(executor.evm.getGasCalculator());
    executor.contractValidationRules = List.of(MaxCodeSizeRule.of(0x6000));
    return executor;
  }

  /**
   * Instantiate Istanbul evm executor.
   *
   * @param evmConfiguration the evm configuration
   * @return the evm executor
   */
  public static Evm istanbul(final EvmConfiguration evmConfiguration) {
    final Evm executor = new Evm(MainnetEVMs.istanbul(evmConfiguration));
    executor.precompileContractRegistry =
        MainnetPrecompiledContracts.istanbul(executor.evm.getGasCalculator());
    executor.contractValidationRules = List.of(MaxCodeSizeRule.of(0x6000));
    return executor;
  }

  /**
   * Instantiate Berlin evm executor.
   *
   * @param evmConfiguration the evm configuration
   * @return the evm executor
   */
  public static Evm berlin(final EvmConfiguration evmConfiguration) {
    final Evm executor = new Evm(MainnetEVMs.berlin(evmConfiguration));
    executor.precompileContractRegistry =
        MainnetPrecompiledContracts.istanbul(executor.evm.getGasCalculator());
    executor.contractValidationRules = List.of(MaxCodeSizeRule.of(0x6000));
    return executor;
  }

  /**
   * Instantiate London evm executor.
   *
   * @param evmConfiguration the evm configuration
   * @return the evm executor
   */
  public static Evm london(final EvmConfiguration evmConfiguration) {
    final Evm executor = new Evm(MainnetEVMs.london(evmConfiguration));
    executor.precompileContractRegistry =
        MainnetPrecompiledContracts.istanbul(executor.evm.getGasCalculator());
    return executor;
  }

  private MessageCallProcessor thisMessageCallProcessor() {
    return Objects.requireNonNullElseGet(
        messageCallProcessor, () -> new MessageCallProcessor(evm, precompileContractRegistry));
  }

  private ContractCreationProcessor thisContractCreationProcessor() {
    return Objects.requireNonNullElseGet(
        contractCreationProcessor,
        () ->
            new ContractCreationProcessor(
                evm.getGasCalculator(),
                evm,
                requireDeposit,
                contractValidationRules,
                initialNonce,
                forceCommitAddresses));
  }

  /**
   * Execute code.
   *
   * @param code the code
   * @param inputData the input data
   * @param value the value
   * @param receiver the receiver
   * @return the bytes
   */
  public MessageFrame execute(
      final Code code, final Bytes inputData, final Wei value, final Address receiver) {
    this.code = code;
    this.callData = inputData;
    this.ethValue = value;
    this.receiver = receiver;
    return execute();
  }

  /**
   * Execute code.
   *
   * @param codeBytes the code bytes
   * @param inputData the input data
   * @param value the value
   * @param receiver the receiver
   * @return the bytes
   */
  public MessageFrame execute(
      final Bytes codeBytes, final Bytes inputData, final Wei value, final Address receiver) {
    this.code = evm.getCode(Hash.hash(codeBytes), codeBytes);
    this.callData = inputData;
    this.ethValue = value;
    this.receiver = receiver;
    return execute();
  }

  /**
   * Execute.
   *
   * @return the bytes
   */
  public MessageFrame execute() {
    final MessageCallProcessor mcp = thisMessageCallProcessor();
    final ContractCreationProcessor ccp = thisContractCreationProcessor();
    final Deque<MessageFrame> messageFrameStack = new ArrayDeque<>();
    final MessageFrame initialMessageFrame =
        MessageFrame.builder()
            .type(MessageFrame.Type.MESSAGE_CALL)
            .messageFrameStack(messageFrameStack)
            .worldUpdater(worldUpdater.updater())
            .initialGas(gas)
            .contract(Address.ZERO)
            .address(receiver)
            .originator(sender)
            .sender(sender)
            .gasPrice(gasPriceGWei)
            .inputData(callData)
            .value(ethValue)
            .apparentValue(ethValue)
            .code(code)
            .blockValues(blockValues)
            .depth(0)
            .completer(c -> {})
            .miningBeneficiary(Address.ZERO)
            .blockHashLookup(h -> null)
            .accessListWarmAddresses(accessListWarmAddresses)
            .accessListWarmStorage(accessListWarmStorage)
            .build();
    messageFrameStack.add(initialMessageFrame);

    while (!messageFrameStack.isEmpty()) {
      final MessageFrame messageFrame = messageFrameStack.peek();
      if (messageFrame.getType() == MessageFrame.Type.CONTRACT_CREATION) {
        ccp.process(messageFrame, tracer);
      } else if (messageFrame.getType() == MessageFrame.Type.MESSAGE_CALL) {
        mcp.process(messageFrame, tracer);
      }
    }
    if (commitWorldState) {
      worldUpdater.commit();
    }
    return initialMessageFrame;
  }

  /**
   * Deploy smart contract.
   *
   * @return the addres of the new smart contract
   */
  public Address deploySmartContract(Account sender, Bytes codeBytes, final Wei value) {

    this.code = evm.getCode(null, codeBytes);
    this.ethValue = value;

    final Address contractAddress = Address.contractAddress(sender.getAddress(), sender.getNonce() + 1);

    final MessageCallProcessor mcp = thisMessageCallProcessor();
    final ContractCreationProcessor ccp = thisContractCreationProcessor();
    final Deque<MessageFrame> messageFrameStack = new ArrayDeque<>();
    final MessageFrame initialMessageFrame =
        MessageFrame.builder()
            .type(MessageFrame.Type.CONTRACT_CREATION)
            .messageFrameStack(messageFrameStack)
            .worldUpdater(worldUpdater.updater())
            .initialGas(gas)
            .contract(contractAddress)
            .address(contractAddress)
            .originator(sender.getAddress())
            .sender(sender.getAddress())
            .gasPrice(gasPriceGWei)
            .inputData(Bytes.EMPTY)
            .value(ethValue)
            .apparentValue(ethValue)
            .code(code)
            .blockValues(blockValues)
            .depth(0)
            .completer(c -> {})
            .miningBeneficiary(Address.ZERO)
            .blockHashLookup(h -> null)
            .accessListWarmAddresses(accessListWarmAddresses)
            .accessListWarmStorage(accessListWarmStorage)
            .build();
    messageFrameStack.add(initialMessageFrame);

    while (!messageFrameStack.isEmpty()) {
      final MessageFrame messageFrame = messageFrameStack.peek();
      if (messageFrame.getType() == MessageFrame.Type.CONTRACT_CREATION) {
        ccp.process(messageFrame, tracer);
      } else if (messageFrame.getType() == MessageFrame.Type.MESSAGE_CALL) {
        mcp.process(messageFrame, tracer);
      }
    }
    if (commitWorldState) {
      worldUpdater.commit();
    }
    return contractAddress;
  }

  /**
   * MArk Commit world state to true.
   *
   * @return the evm executor
   */
  public Evm commitWorldState() {
    this.commitWorldState = true;
    return this;
  }

  /**
   * Sets Commit world state.
   *
   * @param commitWorldState the commit world state
   * @return the evm executor
   */
  public Evm commitWorldState(final boolean commitWorldState) {
    this.commitWorldState = commitWorldState;
    return this;
  }

  /**
   * Sets World updater.
   *
   * @param worldUpdater the world updater
   * @return the evm executor
   */
  public Evm worldUpdater(final WorldUpdater worldUpdater) {
    this.worldUpdater = worldUpdater;
    return this;
  }

  /**
   * Sets Gas.
   *
   * @param gas the gas
   * @return the evm executor
   */
  public Evm gas(final long gas) {
    this.gas = gas;
    return this;
  }

  /**
   * Sets Receiver address.
   *
   * @param receiver the receiver
   * @return the evm executor
   */
  public Evm receiver(final Address receiver) {
    this.receiver = receiver;
    return this;
  }

  /**
   * Sets Sender address.
   *
   * @param sender the sender
   * @return the evm executor
   */
  public Evm sender(final Address sender) {
    this.sender = sender;
    return this;
  }

  /**
   * Sets Gas price GWei.
   *
   * @param gasPriceGWei the gas price g wei
   * @return the evm executor
   */
  public Evm gasPriceGWei(final Wei gasPriceGWei) {
    this.gasPriceGWei = gasPriceGWei;
    return this;
  }

  /**
   * Sets Call data.
   *
   * @param callData the call data
   * @return the evm executor
   */
  public Evm callData(final Bytes callData) {
    this.callData = callData;
    return this;
  }

  /**
   * Sets Eth value.
   *
   * @param ethValue the eth value
   * @return the evm executor
   */
  public Evm ethValue(final Wei ethValue) {
    this.ethValue = ethValue;
    return this;
  }

  /**
   * Sets Code.
   *
   * @param code the code
   * @return the evm executor
   */
  public Evm code(final Code code) {
    this.code = code;
    return this;
  }

  /**
   * Sets Code.
   *
   * @param codeBytes the code bytes
   * @param hash the hash
   * @return the evm executor
   */
  public Evm code(final Bytes codeBytes, final Hash hash) {
    this.code = evm.getCode(hash, codeBytes);
    return this;
  }

  /**
   * Sets Block values.
   *
   * @param blockValues the block values
   * @return the evm executor
   */
  public Evm blockValues(final BlockValues blockValues) {
    this.blockValues = blockValues;
    return this;
  }

  /**
   * Sets Operation Tracer.
   *
   * @param tracer the tracer
   * @return the evm executor
   */
  public Evm tracer(final OperationTracer tracer) {
    this.tracer = tracer;
    return this;
  }

  /**
   * Sets Precompile contract registry.
   *
   * @param precompileContractRegistry the precompile contract registry
   * @return the evm executor
   */
  public Evm precompileContractRegistry(
      final PrecompileContractRegistry precompileContractRegistry) {
    this.precompileContractRegistry = precompileContractRegistry;
    return this;
  }

  /**
   * Sets Require deposit.
   *
   * @param requireDeposit the require deposit
   * @return the evm executor
   */
  public Evm requireDeposit(final boolean requireDeposit) {
    this.requireDeposit = requireDeposit;
    return this;
  }

  /**
   * Sets Initial nonce.
   *
   * @param initialNonce the initial nonce
   * @return the evm executor
   */
  public Evm initialNonce(final long initialNonce) {
    this.initialNonce = initialNonce;
    return this;
  }

  /**
   * Sets Contract validation rules.
   *
   * @param contractValidationRules the contract validation rules
   * @return the evm executor
   */
  public Evm contractValidationRules(
      final List<ContractValidationRule> contractValidationRules) {
    this.contractValidationRules = contractValidationRules;
    return this;
  }

  /**
   * List of EIP-718 contracts that require special delete handling. By default, this is only the
   * RIPEMD precompile contract.
   *
   * @param forceCommitAddresses collection of addresses for special handling
   * @return fluent executor
   * @see <a
   *     href="https://github.com/ethereum/EIPs/issues/716">https://github.com/ethereum/EIPs/issues/716</a>
   */
  public Evm forceCommitAddresses(final Collection<Address> forceCommitAddresses) {
    this.forceCommitAddresses = forceCommitAddresses;
    return this;
  }

  /**
   * Sets Access list warm addresses.
   *
   * @param accessListWarmAddresses the access list warm addresses
   * @return the evm executor
   */
  public Evm accessListWarmAddresses(final Set<Address> accessListWarmAddresses) {
    this.accessListWarmAddresses = accessListWarmAddresses;
    return this;
  }

  /**
   * Sets Warm addresses.
   *
   * @param addresses the addresses
   * @return the evm executor
   */
  public Evm warmAddress(final Address... addresses) {
    this.accessListWarmAddresses.addAll(List.of(addresses));
    return this;
  }

  /**
   * Sets Access list warm storage map.
   *
   * @param accessListWarmStorage the access list warm storage
   * @return the evm executor
   */
  public Evm accessListWarmStorage(final Multimap<Address, Bytes32> accessListWarmStorage) {
    this.accessListWarmStorage = accessListWarmStorage;
    return this;
  }

  /**
   * Sets Access list warm storage.
   *
   * @param address the address
   * @param slots the slots
   * @return the evm executor
   */
  public Evm accessListWarmStorage(final Address address, final Bytes32... slots) {
    this.accessListWarmStorage.putAll(address, List.of(slots));
    return this;
  }

  /**
   * Sets Message call processor.
   *
   * @param messageCallProcessor the message call processor
   * @return the evm executor
   */
  public Evm messageCallProcessor(final MessageCallProcessor messageCallProcessor) {
    this.messageCallProcessor = messageCallProcessor;
    return this;
  }

  /**
   * Sets Contract call processor.
   *
   * @param contractCreationProcessor the contract creation processor
   * @return the evm executor
   */
  public Evm contractCallProcessor(
      final ContractCreationProcessor contractCreationProcessor) {
    this.contractCreationProcessor = contractCreationProcessor;
    return this;
  }
}
