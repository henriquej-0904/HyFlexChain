'use strict';

const {KeyObject} = require("node:crypto");

/**
 * A key pair of public/private keys
 */
class KeyPair
{
	/**
	 * 
	 * @param {KeyObject} pubKey 
	 * @param {KeyObject} privKey 
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