'use strict';

const CryptoUtils = require( "../util/crypto/Crypto").default;
const crypto = require("node:crypto");

const Util = require('../util/Util').default;

//import { Buffer } from 'buffer';

/**
 * Represents a Blockmess Crypto Node Transaction
 */
class BlockmessTransaction
{
	/**
	 * Create a new transaction
	 * @param {string} origin 
	 * @param {string} dest 
	 * @param {Number} value 
	 */
	constructor(origin, dest, value)
	{
		this.origin = Buffer.from(origin, "utf-8");
		this.dest = Buffer.from(dest, "utf-8");
		this.value = value;
		this.nonce = Util.getRandomInt(0, Number.MAX_SAFE_INTEGER);
		this.signature = undefined;
	}

	/**
	 * @return the origin
	 */
	getOrigin() {
		return this.origin;
	}

	/**
	 * @return the dest
	 */
	getDest() {
		return this.dest;
	}

	/**
	 * @return the signature
	 */
	getSignature() {
		return this.signature;
	}

	/**
	 * the value
	 */
	getValue() {
		return this.value;
	}

	/**
	 * @return the nonce
	 */
	getNonce() {
		return this.nonce;
	}


	/**
	 * Sign transaction
	 * @param {crypto.KeyObject} privKey 
	 * @param {CryptoUtils} cryptoUtils 
	 */
	sign(privKey, cryptoUtils)
	{
		let sig = cryptoUtils.getSigInstance();
		sig.update(this.origin);
		sig.update(this.dest);

		let buff = Buffer.alloc(4 + 8);
		buff.writeInt32BE(this.value);
		buff.writeBigInt64BE(this.nonce);

		sig.update(buff);

		sig.end();

		this.signature = sig.sign(privKey);
	}
	
}

module.exports = BlockmessTransaction;