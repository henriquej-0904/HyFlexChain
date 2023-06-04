package pt.unl.fct.di.hyflexchain.evm.contract.examples;

import java.util.Arrays;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.evm.Code;
import org.hyperledger.besu.evm.account.EvmAccount;
import org.hyperledger.besu.evm.fluent.SimpleWorld;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.hyperledger.besu.evm.worldstate.WorldUpdater;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;

import pt.unl.fct.di.hyflexchain.evm.contract.SmartContract;
import pt.unl.fct.di.hyflexchain.evm.executor.EvmExecutor;

public class HelloWorldContract extends SmartContract {
    
    public static final String FUNC_GETGREETING = "getGreeting";
    
    private SmartContract contract;

    public HelloWorldContract(SmartContract contract) {
        super();
        this.contract = contract;
    }

    @Override
    public Address getContractAddress() {
        return contract.getContractAddress();
    }

    @Override
    public Code getCode() {
        return contract.getCode();
    }

    public String getGreeting(final EvmExecutor evm, final Address sender,
        final WorldUpdater worldUpdater) {
        final Function function = new Function(FUNC_GETGREETING, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeCallSingleValueReturn(evm, sender, function, String.class, worldUpdater);
    }

    public static void main(String[] args) {
		try {
			final Bytes deployCodeBytes = Bytes.fromHexString(args[0]);

			final WorldUpdater updater = new SimpleWorld();

			final EvmAccount senderAccount =
				updater.createAccount(Address.wrap(Bytes.random(20)), 0, Wei.MAX_WEI);
			final Address sender = senderAccount.getAddress();

			final EvmExecutor exec = EvmExecutor.london(EvmConfiguration.DEFAULT);
			// exec.tracer(new StandardJsonTracer(System.out, false, true, true));
			

			final HelloWorldContract contract = new HelloWorldContract(
                exec.deploySmartContract(sender, deployCodeBytes, Wei.ZERO, updater)
            );
			System.out.println("Smart contract deployed address: " + contract.getContractAddress().toHexString());

            final String result = contract.getGreeting(exec, sender, updater);
			System.out.printf("Smart contract exec %s: %s\n", FUNC_GETGREETING, result);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
