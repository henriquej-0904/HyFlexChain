'use strict';

const { generateKeyPairSync, createPublicKey, createPrivateKey, createSign, createHash, Hash } = require('node:crypto');
const KeyPair = require("./KeyPair");
const Buffer = require('buffer').Buffer;

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
				namedCurve: "secp521r1"
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
		let res = pubKey.export(
			{
				type: "spki",
				format: "der"
			}
		);

		return res.toString('hex');
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
	 * @returns {object[]} an encoded format of the key
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
		let key = Buffer.from(pubKey, 'hex');

		return createPublicKey(
			{
				key: key,
				type: "spki",
				format: "der"
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
		// AKA ECDSA-with-SHA256
		return createSign("SHA256");
	}

	/**
	 * 
	 * @returns {Hash} hash instance
	 */
	getHashInstance()
	{
		return createHash("sha256");
	}

}

module.exports = Crypto;