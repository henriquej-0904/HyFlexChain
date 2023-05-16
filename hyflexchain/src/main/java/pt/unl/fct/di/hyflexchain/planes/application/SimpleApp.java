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

import pt.unl.fct.di.hyflexchain.planes.application.ti.InvalidTransactionException;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionId;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TxInput;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.UTXO;
import pt.unl.fct.di.hyflexchain.util.Crypto;

public class SimpleApp extends ApplicationInterface
{
	private final SecureRandom rand;

	private final KeyPair keyPair;

	private final String address;

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
		this.address = Address.getAddress(this.keyPair.getPublic());

		this.nonce = 0L;
	}

	public HyFlexChainTransaction createTx()
	{
		HyFlexChainTransaction tx = new HyFlexChainTransaction();
		tx.setVersion(HyFlexChainTransaction.Version.V1_0.toString());
		tx.setAddress(this.address);
		tx.setSignatureType(Crypto.DEFAULT_SIGNATURE_TRANSFORMATION);
		tx.setNonce(nonce++);
		tx.setInputTxs(new TxInput[]{
			new TxInput(
				new TransactionId("sender", "hash"),
				0
			)
		});
		tx.setOutputTxs(new UTXO[]{
			new UTXO("address", 45)
		});
		tx.setData(new byte[] {0});

		try {
			tx.sign(this.keyPair.getPrivate(), tx.getSignatureType());
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
			e.printStackTrace();
		}
		
		tx.setHash(tx.hash());

		return tx;
	}

	public void submitTxAndWait()
	{
		HyFlexChainTransaction tx = createTx();
		
		System.out.println("Submiting tx and waiting for block finalization");

		try {
			this.ti.sendTransactionAndWait(tx);
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
				this.ti.sendTransaction(tx);
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

		System.out.println("Waiting for input");

		try (Scanner sc = new Scanner(System.in);)
		{
			while (true) {
				switch (sc.nextInt()) {
					case 0:
						app.submitTxAndWait();
						break;
					case 1:
						app.submitTxs(300000);
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
