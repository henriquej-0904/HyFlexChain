'use strict';

import { generateKeyPairSync, createPublicKey, createPrivateKey, createSign } from "node:crypto";
import KeyPair from "./KeyPair";

/**
 * A crypto utils class
 */
class Crypto
{
	PASSPHRASE = "top secret";

	/**
	 * Generate a EC key pair.
	 * @returns {KeyPair} key pair
	 */
	genKeyPairEC() {
		let res = generateKeyPairSync(
			"ec",
			{
				namedCurve: "secp256r1"
			}
		);

		return new KeyPair(res.publicKey, res.privateKey);
	}

	/**
	 * Encode a public key
	 * @param {crypto.KeyObject} pubKey 
	 * @returns {string} an encoded format of the key
	 */
	encodePublicKey(pubKey)
	{
		return pubKey.export(
			{
				type: "spki",
				format: "pem"
			}
		);
	}

	/**
	 * Encode a private key
	 * @param {crypto.KeyObject} privKey 
	 * @returns {string} an encoded format of the key
	 */
	encodePrivateKey(privKey)
	{
		return privKey.export(
			{
				type: "pkcs8",
				format: "pem",
				cipher: 'aes-256-cbc',
    			passphrase: this.PASSPHRASE,
			}
		);
	}

	/**
	 * Encode a key pair
	 * @param {KeyPair} keyPair 
	 * @returns {string[]} an encoded format of the key
	 */
	encodeKeyPair(keyPair)
	{
		return [
			this.encodePublicKey(keyPair.getPublicKey()),
			this.encodePrivateKey(keyPair.getPrivateKey())
		];
	}


	/**
	 * Decode a public key
	 * @param {string} pubKey 
	 * @returns {crypto.KeyObject} the key object
	 */
	decodePublicKey(pubKey)
	{
		return createPublicKey(
			{
				key: pubKey,
				type: "spki",
				format: "pem"
			}
		);
	}

	/**
	 * Decode a private key
	 * @param {string} privKey 
	 * @returns {crypto.KeyObject} the key object
	 */
	decodePrivateKey(privKey)
	{
		return createPrivateKey(
			{
				key: privKey,
				type: "pkcs8",
				format: "pem",
				cipher: 'aes-256-cbc',
    			passphrase: this.PASSPHRASE,
			}
		);
	}

	/**
	 * Decode a key pair
	 * @param {string[]} keyPair 
	 * @returns {KeyPair} the key pair
	 */
	decodeKeyPair(keyPair)
	{
		return new KeyPair(
			this.decodePublicKey(keyPair[0]),
			this.decodePrivateKey(keyPair[1])
		);
	}

	getSigInstance()
	{
		return createSign("SHA256withECDSA");
	}

}

export default Crypto;