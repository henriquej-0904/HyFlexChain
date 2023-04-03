'use strict';

import crypto from "node:crypto";

/**
 * A key pair of public/private keys
 */
class KeyPair
{
	/**
	 * 
	 * @param {crypto.KeyObject} pubKey 
	 * @param {crypto.KeyObject} privKey 
	 */
	constructor(pubKey, privKey)
	{
		this.pubKey = pubKey;
		this.privKey = privKey;
	}

	/**
	 * 
	 * @returns public key
	 */
	getPublicKey()
	{
		return this.pubKey;
	}

	/**
	 * 
	 * @returns private key
	 */
	getPrivateKey()
	{
		return this.privKey;
	}
}

module.exports = KeyPair;