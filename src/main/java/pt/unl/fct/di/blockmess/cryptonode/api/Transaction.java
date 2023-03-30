package pt.unl.fct.di.blockmess.cryptonode.api;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import pt.unl.fct.di.blockmess.cryptonode.util.Crypto;
import pt.unl.fct.di.blockmess.cryptonode.util.Utils;

public class Transaction
{
	private byte[] origin, dest, publicKey, signature;

	private int value;

	private long nonce;

	public Transaction()
	{
		
	}

	/**
	 * @return the origin
	 */
	public byte[] getOrigin() {
		return origin;
	}

	/**
	 * @param origin the origin to set
	 */
	public void setOrigin(byte[] origin) {
		this.origin = origin;
	}

	/**
	 * @return the dest
	 */
	public byte[] getDest() {
		return dest;
	}

	/**
	 * @param dest the dest to set
	 */
	public void setDest(byte[] dest) {
		this.dest = dest;
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
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(int value) {
		this.value = value;
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
	 * @return the publicKey
	 */
	public byte[] getPublicKey() {
		return publicKey;
	}

	/**
	 * @param publicKey the publicKey to set
	 */
	public void setPublicKey(byte[] publicKey) {
		this.publicKey = publicKey;
	}


	public byte[] sign(PrivateKey key) throws InvalidKeyException, SignatureException
	{
		Signature sig = Crypto.createSignatureInstance();
		sig.initSign(key);
		sig.update(this.origin);
		sig.update(this.dest);
		sig.update(Utils.toBytes(this.value));
		sig.update(Utils.toBytes(this.nonce));

		this.signature = sig.sign();
		return this.signature;
	}

	public boolean checkSignature() throws InvalidKeyException, SignatureException, InvalidKeySpecException
	{
		Signature sig = Crypto.createSignatureInstance();
		sig.initVerify(Crypto.getPublicKey(this.publicKey));
		sig.update(this.origin);
		sig.update(this.dest);
		sig.update(Utils.toBytes(this.value));
		sig.update(Utils.toBytes(this.nonce));

		return sig.verify(this.signature);
	}

	
	
}