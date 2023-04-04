'use strict';

const CryptoUtils = require( "../util/crypto/Crypto");
const crypto = require("node:crypto");

const Util = require('../util/Util');

const Buffer = require('buffer').Buffer;

/**
 * Represents a Blockmess Crypto Node Transaction
 */
class BlockmessTransaction
{
	/**
	 * Create a new transaction
	 * @param {string} origin 
	 * @param {string} dest 
	 * @param {number} value 
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
		return this.origin.toString('base64');
	}

	/**
	 * @return the dest
	 */
	getDest() {
		return this.dest.toString('base64');
	}

	/**
	 * @return the signature
	 */
	getSignature() {
		return this.signature.toString('base64');
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

		let buff1 = Buffer.alloc(4);
		buff1.writeInt32BE(this.value);
		sig.update(buff1);

		let buff2 = Buffer.alloc(8);
		buff2.writeBigInt64BE(BigInt(this.nonce));
		sig.update(buff2);

		sig.end();

		this.signature = sig.sign(privKey);
	}
	
}

module.exports = BlockmessTransaction;