package pt.unl.fct.di.hyflexchain.evm;

import org.apache.tuweni.bytes.Bytes;

import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.evm.Code;
import org.hyperledger.besu.evm.EVM;
import org.hyperledger.besu.evm.EvmSpecVersion;
import org.hyperledger.besu.evm.account.Account;
import org.hyperledger.besu.evm.account.AccountStorageEntry;
import org.hyperledger.besu.evm.account.EvmAccount;
import org.hyperledger.besu.evm.code.CodeInvalid;
import org.hyperledger.besu.evm.fluent.EVMExecutor;
import org.hyperledger.besu.evm.fluent.SimpleWorld;
import org.hyperledger.besu.evm.frame.MessageFrame;
import org.hyperledger.besu.evm.gascalculator.GasCalculator;
import org.hyperledger.besu.evm.gascalculator.LondonGasCalculator;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.hyperledger.besu.evm.log.LogsBloomFilter;
import org.hyperledger.besu.evm.operation.OperationRegistry;
import org.hyperledger.besu.evm.precompile.PrecompileContractRegistry;
import org.hyperledger.besu.evm.processor.MessageCallProcessor;
import org.hyperledger.besu.evm.tracing.OperationTracer;
import org.hyperledger.besu.evm.tracing.StandardJsonTracer;
import org.hyperledger.besu.evm.worldstate.WorldState;
import org.hyperledger.besu.evm.worldstate.WorldUpdater;
import org.slf4j.LoggerFactory;

public class ExecEVM {
	public static void main(String[] args) {
		try {
			/*
			 * final EvmToolComponent component = DaggerEvmToolComponent.builder()
			 * .dataStoreModule(new DataStoreModule())
			 * .genesisFileModule(GenesisFileModule.createGenesisModule(NetworkName.DEV))
			 * .evmToolCommandOptionsModule(daggerOptions)
			 * .metricsSystemModule(new MetricsSystemModule())
			 * .build();
			 * 
			 * final BlockHeader blockHeader = BlockHeaderBuilder.create()
			 * .parentHash(Hash.EMPTY)
			 * .coinbase(Address.ZERO)
			 * .difficulty(Difficulty.ONE)
			 * .number(1)
			 * .gasLimit(5000)
			 * .timestamp(Instant.now().toEpochMilli())
			 * .ommersHash(Hash.EMPTY_LIST_HASH)
			 * .stateRoot(Hash.EMPTY_TRIE_HASH)
			 * .transactionsRoot(Hash.EMPTY)
			 * .receiptsRoot(Hash.EMPTY)
			 * .logsBloom(LogsBloomFilter.empty())
			 * .gasUsed(0)
			 * .extraData(Bytes.EMPTY)
			 * .mixHash(Hash.EMPTY)
			 * .nonce(0)
			 * .blockHeaderFunctions(new MainnetBlockHeaderFunctions())
			 * .buildBlockHeader();
			 */

			final Bytes deployCodeBytes = Bytes.fromHexString(args[0]);

			final WorldUpdater updater = new SimpleWorld();

			final EvmAccount senderAccount =
				updater.createAccount(Address.wrap(Bytes.random(20)), 0, Wei.MAX_WEI);
			final Address sender = senderAccount.getAddress();

			/* final EvmAccount mutContractAccount =
				updater.createAccount(Address.contractAddress(sender, 0), 0, Wei.ONE);

			mutContractAccount.getMutable().setCode(codeBytes);

			final Address contract = mutContractAccount.getAddress(); */

			final Evm exec = Evm.london(EvmConfiguration.DEFAULT);
			exec.worldUpdater(updater);
			exec.tracer(new StandardJsonTracer(System.out, false, true, true));

			exec.sender(sender);

			// final Address contractAddress = Address.contractAddress(sender, 0);
			
			final Address contractAddress = exec.deploySmartContract(senderAccount, deployCodeBytes, Wei.ZERO);
			System.out.println("Smart contract deploy address: " + contractAddress.toHexString());

			final EvmAccount contractAccount = updater.getAccount(contractAddress);

			for (int i = 1; i < args.length; i++)
			{
				final Bytes callDataBytes = Bytes.fromHexString(args[i]);
				exec.sender(sender); 
				final MessageFrame result = exec.execute(contractAccount.getCode(), callDataBytes, Wei.ZERO, contractAddress);
				
				System.out.println("Smart contract exec result: " + result.getOutputData().toHexString());
			}

			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	

}
