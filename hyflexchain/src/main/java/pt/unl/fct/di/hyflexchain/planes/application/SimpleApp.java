package pt.unl.fct.di.hyflexchain.planes.application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.Scanner;

import org.apache.commons.cli.ParseException;
import org.apache.tuweni.bytes.Bytes;
import org.slf4j.LoggerFactory;

import pt.unl.fct.di.hyflexchain.planes.application.ti.InvalidTransactionException;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TxInput;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.UTXO;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.wrapper.TxWrapper;
import pt.unl.fct.di.hyflexchain.util.crypto.Crypto;

public class SimpleApp extends ApplicationInterface
{
	private final SecureRandom rand;

	private final KeyPair keyPair;

	private final Address address;

	private long nonce;


	public SimpleApp(File configFolder, String[] overridenConfigs)
			throws FileNotFoundException, IOException, ParseException {
		super(configFolder, overridenConfigs);

		try {
			this.rand = SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
			throw new Error(e);
		}

		this.keyPair = Crypto.createKeyPairForEcc256bits(this.rand);
		this.address = Address.fromPubKey(this.keyPair.getPublic());

		this.nonce = 0L;
	}

	public HyFlexChainTransaction createTx()
	{
		HyFlexChainTransaction tx = new HyFlexChainTransaction();
		tx.setVersion(HyFlexChainTransaction.Version.V1_0.toString());
		tx.setSender(this.address);
		tx.setSignatureType(Crypto.DEFAULT_SIGNATURE_TRANSFORMATION);
		tx.setNonce(nonce++);
		tx.setInputTxs(new TxInput[]{
			new TxInput(
				Bytes.random(256/8).toArrayUnsafe(),
				0
			)
		});
		tx.setOutputTxs(new UTXO[]{
			new UTXO(address, 45)
		});

		byte[] bytes = new byte[20];
		rand.nextBytes(bytes);
		tx.setData(bytes);

		try {
			tx.sign(this.keyPair.getPrivate(), tx.getSignatureType());
		} catch (InvalidKeyException | SignatureException e) {
			e.printStackTrace();
		}

		return tx;
	}

	public void submitTxAndWait()
	{
		HyFlexChainTransaction tx = createTx();
		
		System.out.println("Submiting tx and waiting for block finalization");

		try {
			this.ti.sendTransactionAndWait(TxWrapper.from(tx));
		} catch (InvalidTransactionException e) {
			e.printStackTrace();
		}

		System.out.println("Submited tx");
	}

	public void submitTxs(int n)
	{
		for (int i = 0; i < n; i++) {
			HyFlexChainTransaction tx = createTx();
			try {
				this.ti.sendTransaction(TxWrapper.from(tx));
			} catch (InvalidTransactionException e) {
				e.printStackTrace();
			}
		}

		System.out.println(String.format("Submited %d txs", n));
	}

	public void printLedger()
	{
		var ledger = this.lvi.getLedger(ConsensusMechanism.PoW);
		System.out.println(ledger);
	}

	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
		SimpleApp app = new SimpleApp(new File(args[0]), args);

		LoggerFactory.getLogger(SimpleApp.class).info("Ready...");

		try (Scanner sc = new Scanner(System.in);)
		{
			while (true) {
				switch (sc.nextInt()) {
					case 0:
						app.submitTxAndWait();
						break;
					case 1:
						app.submitTxs(20000);
						break;
					case 2:
						app.printLedger();
						break;
					default:
						break;
				}
	
				sc.nextLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
