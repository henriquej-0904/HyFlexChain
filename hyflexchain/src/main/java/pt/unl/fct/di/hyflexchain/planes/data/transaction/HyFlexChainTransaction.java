package pt.unl.fct.di.hyflexchain.planes.data.transaction;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.function.Consumer;

import pt.unl.fct.di.hyflexchain.util.Bytes;
import pt.unl.fct.di.hyflexchain.util.Crypto;
import pt.unl.fct.di.hyflexchain.util.Utils;

/**
 * Represents a HyFlexChain Transaction
 */
public class HyFlexChainTransaction {

	/**
	 * The version of the transaction.
	 */
	protected String version;

	/**
	 * The hash of the transaction
	 */
	protected String hash;

	/**
	 * The address of the sender, that will be signing the transaction.
	 */
	protected String address;

	/**
	 * The signature algorithm.
	 */
	protected String signatureType;

	/**
	 * The identifier of the sender.
	 * This is generated when the sender's private key signs the transaction
	 * and confirms the sender has authorized this transaction
	 */
	protected String signature;

	/**
	 * A sequentially incrementing counter which indicates the transaction number from the account
	 */
	protected long nonce;

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

	//TODO: Add gas to transaction

	//TODO smart contract (codigo, referencia)


	/**
	 * Create a transaction
	 */
	public HyFlexChainTransaction() {
	}

	public String sign(PrivateKey key, String signatureAlg) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException
    {
        Signature signature = Crypto.createSignatureInstance(signatureAlg);
        signature.initSign(key);
        
        signature.update(this.version.getBytes());
		signature.update(this.address.getBytes());
		signature.update(Utils.toBytes(this.nonce));

		final Consumer<ByteBuffer> apply = (buff) -> {
			try {
				signature.update(buff);
			} catch (SignatureException e) {
				// never happens
				e.printStackTrace();
			}
		};

		Bytes.applyToBytes(this.inputTxs, apply);
		Bytes.applyToBytes(this.outputTxs, apply);

		signature.update(this.data);

        var sigRes = Utils.toHex(signature.sign());

		this.signatureType = signatureAlg;
		this.signature = sigRes;

		return sigRes;
    }

	public boolean verifySignature() throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, SignatureException
    {
        var key = Address.readPublicKey(this.address);
		var sigAlg = this.signatureType;
		return verifySignature(key, sigAlg);
    }

	protected boolean verifySignature(PublicKey key, String sigAlg) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException
    {
        Signature signature = Crypto.createSignatureInstance(sigAlg);
        signature.initVerify(key);
        
        signature.update(this.version.getBytes());
		signature.update(this.address.getBytes());
		signature.update(Utils.toBytes(this.nonce));

		final Consumer<ByteBuffer> apply = (buff) -> {
			try {
				signature.update(buff);
			} catch (SignatureException e) {
				// never happens
				e.printStackTrace();
			}
		};

		Bytes.applyToBytes(this.inputTxs, apply);
		Bytes.applyToBytes(this.outputTxs, apply);

		signature.update(this.data);

        var sigBytes = Utils.fromHex(this.signature);
		return signature.verify(sigBytes);
    }

	public String hash()
	{
		var msgDigest = Crypto.getSha256Digest();
        
        msgDigest.update(this.version.getBytes());
		msgDigest.update(this.address.getBytes());
		msgDigest.update(this.signatureType.getBytes());
		msgDigest.update(this.signature.getBytes());
		msgDigest.update(Utils.toBytes(this.nonce));

		final Consumer<ByteBuffer> apply = msgDigest::update;

		Bytes.applyToBytes(this.inputTxs, apply);
		Bytes.applyToBytes(this.outputTxs, apply);

		msgDigest.update(this.data);

		return Utils.toHex(msgDigest.digest());
	}

	public boolean verifyHash()
	{
		return this.hash.equalsIgnoreCase(hash());
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
	 * @return the hash
	 */
	public String getHash() {
		return hash;
	}

	/**
	 * @param hash the hash to set
	 */
	public void setHash(String hash) {
		this.hash = hash;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the signatureType
	 */
	public String getSignatureType() {
		return signatureType;
	}

	/**
	 * @param signatureType the signatureType to set
	 */
	public void setSignatureType(String signatureType) {
		this.signatureType = signatureType;
	}

	/**
	 * @return the signature
	 */
	public String getSignature() {
		return signature;
	}

	/**
	 * @param signature the signature to set
	 */
	public void setSignature(String signature) {
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
	
}
