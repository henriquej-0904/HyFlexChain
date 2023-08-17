package pt.unl.fct.di.hyflexchain.planes.data.transaction;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import pt.unl.fct.di.hyflexchain.util.BytesOps;
import pt.unl.fct.di.hyflexchain.util.Utils;
import pt.unl.fct.di.hyflexchain.util.crypto.Crypto;
import pt.unl.fct.di.hyflexchain.util.crypto.HashOps;
import pt.unl.fct.di.hyflexchain.util.crypto.HyFlexChainSignature;
import pt.unl.fct.di.hyflexchain.util.crypto.SignatureAlgorithm;
import pt.unl.fct.di.hyflexchain.util.crypto.SignatureOps;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

/**
 * Represents a HyFlexChain Transaction
 */
public class HyFlexChainTransaction implements BytesOps, HashOps, SignatureOps {

	public static final Serializer SERIALIZER = new Serializer();

	/**
	 * The version of the transaction.
	 */
	protected String version;

	/**
	 * The address of the sender, that will be signing the transaction.
	 */
	protected Address sender;

	/**
	 * The signature algorithm.
	 */
	protected SignatureAlgorithm signatureType;

	/**
	 * The identifier of the sender.
	 * This is generated when the sender's private key signs the transaction
	 * and confirms the sender has authorized this transaction
	 */
	protected byte[] signature;

	/**
	 * A sequentially incrementing counter which indicates the transaction number from the account
	 */
	protected long nonce;

	/**
	 * The type of this transaction
	 */
	protected TransactionType transactionType;

	/**
	 * A smart contract to create, revoke or execute.
	 */
	protected SmartContract smartContract;

	/**
	 * The input transactions
	 */
	protected TxInput[] inputTxs;

	/**
	 * The output transactions
	 */
	protected UTXO[] outputTxs;

	/**
	 * Optional field to include arbitrary data
	 */
	protected byte[] data;

	/**
	 * @param sender
	 * @param signatureType
	 * @param signature
	 * @param nonce
	 * @param transactionType
	 * @param smartContract
	 * @param inputTxs
	 * @param outputTxs
	 * @param data
	 */
	public static HyFlexChainTransaction create(Address sender, SignatureAlgorithm signatureType, byte[] signature,
			long nonce, TransactionType transactionType, SmartContract smartContract, TxInput[] inputTxs,
			UTXO[] outputTxs, byte[] data) {
		return new HyFlexChainTransaction(Version.V1_0.getVersion(), sender, signatureType, signature,
			nonce, transactionType, smartContract, inputTxs, outputTxs, data);
	}

	/**
	 * @param sender
	 * @param signatureType
	 * @param signature
	 * @param nonce
	 * @param smartContract
	 */
	public static HyFlexChainTransaction createContract(Address sender, SignatureAlgorithm signatureType, byte[] signature,
			long nonce, SmartContract smartContract) {
		return new HyFlexChainTransaction(Version.V1_0.getVersion(), sender, signatureType, signature,
			nonce, TransactionType.CONTRACT_CREATE, smartContract, new TxInput[0], new UTXO[0], new byte[0]);
	}

	/**
	 * @param sender
	 * @param signatureType
	 * @param signature
	 * @param nonce
	 * @param smartContract
	 */
	public static HyFlexChainTransaction revokeContract(Address sender, SignatureAlgorithm signatureType, byte[] signature,
			long nonce, SmartContract smartContract) {
		return new HyFlexChainTransaction(Version.V1_0.getVersion(), sender, signatureType, signature,
			nonce, TransactionType.CONTRACT_REVOKE, smartContract, new TxInput[0], new UTXO[0], new byte[0]);
	}

	/**
	 * Create a transaction
	 */
	public HyFlexChainTransaction() {
	}

	

	/**
	 * @param version
	 * @param sender
	 * @param signatureType
	 * @param signature
	 * @param nonce
	 * @param transactionType
	 * @param smartContract
	 * @param inputTxs
	 * @param outputTxs
	 * @param data
	 */
	public HyFlexChainTransaction(String version, Address sender, SignatureAlgorithm signatureType, byte[] signature,
			long nonce, TransactionType transactionType, SmartContract smartContract, TxInput[] inputTxs,
			UTXO[] outputTxs, byte[] data) {
		this.version = version;
		this.sender = sender;
		this.signatureType = signatureType;
		this.signature = signature;
		this.nonce = nonce;
		this.transactionType = transactionType;
		this.smartContract = smartContract;
		this.inputTxs = inputTxs;
		this.outputTxs = outputTxs;
		this.data = data;
	}


	public byte[] sign(PrivateKey key, SignatureAlgorithm signatureAlg) throws InvalidKeyException, SignatureException
    {
        Signature signature = Crypto.createSignatureInstance(signatureAlg);
        signature.initSign(key);
		update(signature);

		this.signatureType = signatureAlg;
		this.signature = signature.sign();

		return this.signature;
    }

	public boolean verifySignature() throws InvalidAddressException, NoSuchAlgorithmException, InvalidKeyException, SignatureException
    {
        var key = this.sender.readPublicKey();
		var sigAlg = this.signatureType;
		return verifySignature(key, sigAlg);
    }

	public boolean verifySignature(PublicKey key, SignatureAlgorithm sigAlg) throws InvalidKeyException, SignatureException
    {
        Signature signature = Crypto.createSignatureInstance(sigAlg);
        signature.initVerify(key);
		update(signature);

		return signature.verify(this.signature);
    }

	public Address[] recipientAddresses()
	{
		int outputTxsLength = this.outputTxs.length;
		if (outputTxsLength == 0)
			return new Address[0];

		if (outputTxsLength == 1)
			return new Address[] {this.outputTxs[0].recipient()};

		Address[] res = this.outputTxs[outputTxsLength - 1].recipient().equals(this.sender)
			? new Address[outputTxsLength - 1]
			: new Address[outputTxsLength];

		for (int i = 0; i < res.length; i++)
			res[i] = this.outputTxs[i].recipient();

		return res;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the transactionType
	 */
	public TransactionType getTransactionType() {
		return transactionType;
	}

	/**
	 * @param transactionType the transactionType to set
	 */
	public void setTransactionType(TransactionType transactionType) {
		this.transactionType = transactionType;
	}

	/**
	 * @return the address
	 */
	public Address getSender() {
		return sender;
	}

	/**
	 * @param address the address to set
	 */
	public void setSender(Address sender) {
		this.sender = sender;
	}

	/**
	 * @return the signatureType
	 */
	public SignatureAlgorithm getSignatureType() {
		return signatureType;
	}

	/**
	 * @param signatureType the signatureType to set
	 */
	public void setSignatureType(SignatureAlgorithm signatureType) {
		this.signatureType = signatureType;
	}

	/**
	 * @return the signature
	 */
	public byte[] getSignature() {
		return signature;
	}

	/**
	 * @param signature the signature to set
	 */
	public void setSignature(byte[] signature) {
		this.signature = signature;
	}

	/**
	 * @return the nonce
	 */
	public long getNonce() {
		return nonce;
	}

	/**
	 * @param nonce the nonce to set
	 */
	public void setNonce(long nonce) {
		this.nonce = nonce;
	}

	/**
	 * @return the smartContract
	 */
	public SmartContract getSmartContract() {
		return smartContract;
	}

	/**
	 * @param smartContract the smartContract to set
	 */
	public void setSmartContract(SmartContract smartContract) {
		this.smartContract = smartContract;
	}

	/**
	 * @return the inputTxs
	 */
	public TxInput[] getInputTxs() {
		return inputTxs;
	}

	/**
	 * @param inputTxs the inputTxs to set
	 */
	public void setInputTxs(TxInput[] inputTxs) {
		this.inputTxs = inputTxs;
	}

	/**
	 * @return the outputTxs
	 */
	public UTXO[] getOutputTxs() {
		return outputTxs;
	}

	/**
	 * @param outputTxs the outputTxs to set
	 */
	public void setOutputTxs(UTXO[] outputTxs) {
		this.outputTxs = outputTxs;
	}

	/**
	 * @return the data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

	/**
	 * The version of this transaction
	 */
	public static enum Version {
		V1_0("v1.0");

		private String version;

		/**
		 * @param version
		 */
		private Version(String version) {
			this.version = version;
		}

		/**
		 * @return the version
		 */
		public String getVersion() {
			return version;
		}
	}

	public static class Serializer implements ISerializer<HyFlexChainTransaction> {

		protected static final ISerializer<byte[]> byteArraySerializer =
			Utils.serializer.getArraySerializerByte();

		protected static final ISerializer<String> stringSerializer =
			Utils.serializer.getSerializer(String.class);

		protected static final ISerializer<TxInput[]> txInputArraySerializer =
			Utils.serializer.getArraySerializer(TxInput.class, TxInput.SERIALIZER);

		protected static final ISerializer<UTXO[]> txOutputArraySerializer =
			Utils.serializer.getArraySerializer(UTXO.class, UTXO.SERIALIZER);

		public void serializeHeader(HyFlexChainTransaction t, ByteBuf out) throws IOException {
			stringSerializer.serialize(t.version, out);
			HyFlexChainSignature.SERIALIZER.serialize(new HyFlexChainSignature(t.sender, t.signatureType, t.signature), out);
			out.writeLong(t.nonce);
			TransactionType.SERIALIZER.serialize(t.transactionType, out);
		}

		public void serializeBody(HyFlexChainTransaction t, ByteBuf out) throws IOException {
			SmartContract.SERIALIZER.serialize(t.smartContract, out);
			txInputArraySerializer.serialize(t.inputTxs, out);
			txOutputArraySerializer.serialize(t.outputTxs, out);
			byteArraySerializer.serialize(t.data, out);
		}

		@Override
		public void serialize(HyFlexChainTransaction t, ByteBuf out) throws IOException {
			serializeHeader(t, out);
			serializeBody(t, out);
		}

		@Override
		public HyFlexChainTransaction deserialize(ByteBuf in) throws IOException {
			HyFlexChainTransaction t = new HyFlexChainTransaction();

			t.version = stringSerializer.deserialize(in);
			var sig = HyFlexChainSignature.SERIALIZER.deserialize(in);
			t.sender = sig.address();
			t.signatureType = sig.signatureAlg();
			t.signature = sig.signature();
			t.nonce = in.readLong();
			t.transactionType = TransactionType.SERIALIZER.deserialize(in);
			t.smartContract = SmartContract.SERIALIZER.deserialize(in);
			t.inputTxs = txInputArraySerializer.deserialize(in);
			t.outputTxs = txOutputArraySerializer.deserialize(in);
			t.data = byteArraySerializer.deserialize(in);

			return t;
		}
	}

	@Override
	public int serializedSize()
	{
		return BytesOps.serializedSize(version)	
			+ sender.serializedSize()			
			+ signatureType.serializedSize()
			+ BytesOps.serializedSize(signature)
			+ Long.BYTES
			+ transactionType.serializedSize()
			+ smartContract.serializedSize()
			+ BytesOps.serializedSize(inputTxs)
			+ BytesOps.serializedSize(outputTxs)
			+ BytesOps.serializedSize(data);
	}

	@Override
	public Signature update(Signature sig) throws SignatureException {
		sig.update(this.version.getBytes());
		this.sender.update(sig);
		sig.update(Utils.toBytes(this.nonce));
		sig.update(this.transactionType.id);
		this.smartContract.update(sig);
		SignatureOps.updateArray(this.inputTxs, sig);
		SignatureOps.updateArray(this.outputTxs, sig);
		sig.update(this.data);

		return sig;
	}

	@Override
	public MessageDigest update(MessageDigest md) {

		try {
			var buff = Unpooled.buffer(100);
			SERIALIZER.serializeHeader(this, buff);

			buff.forEachByte(0, buff.writerIndex(), (b) -> {
				md.update(b);
				return true;
			});
			buff.clear();

			this.smartContract.update(md);

			Serializer.txInputArraySerializer.serialize(this.inputTxs, buff);
			Serializer.txOutputArraySerializer.serialize(this.outputTxs, buff);

			buff.forEachByte(0, buff.writerIndex(), (b) -> {
				md.update(b);
				return true;
			});
			buff.clear();

			md.update(Utils.toBytes(this.data.length));
			md.update(this.data);

			return md;
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error(e.getMessage(), e);
		}

	}
	
}
