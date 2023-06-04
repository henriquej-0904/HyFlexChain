package pt.unl.fct.di.hyflexchain.evm;

import org.apache.tuweni.bytes.Bytes;

import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.evm.account.EvmAccount;
import org.hyperledger.besu.evm.fluent.SimpleWorld;
import org.hyperledger.besu.evm.frame.MessageFrame;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.hyperledger.besu.evm.tracing.StandardJsonTracer;
import org.hyperledger.besu.evm.worldstate.WorldUpdater;

import pt.unl.fct.di.hyflexchain.evm.contract.SmartContract;
import pt.unl.fct.di.hyflexchain.evm.executor.EvmExecutor;

public class ExecEVM {
	public static void main(String[] args) {
		try {
			final Bytes deployCodeBytes = Bytes.fromHexString(args[0]);

			final WorldUpdater updater = new SimpleWorld();

			final EvmAccount senderAccount =
				updater.createAccount(Address.wrap(Bytes.random(20)), 0, Wei.MAX_WEI);
			final Address sender = senderAccount.getAddress();

			final EvmExecutor exec = EvmExecutor.london(EvmConfiguration.DEFAULT);
			// exec.tracer(new StandardJsonTracer(System.out, false, true, true));
			

			final SmartContract contract = exec.deploySmartContract(sender, deployCodeBytes, Wei.ZERO, updater);
			System.out.println("Smart contract deploy address: " + sender.toHexString());

			/* for (int i = 1; i < args.length; i++)
			{
				final Bytes callDataBytes = Bytes.fromHexString(args[i]);
				exec.sender(sender); 
				final MessageFrame result = exec.execute(contractAccount.getCode(), callDataBytes, Wei.ZERO, contractAddress);
				
				System.out.println("Smart contract exec result: " + result.getOutputData().toHexString());
			} */

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
