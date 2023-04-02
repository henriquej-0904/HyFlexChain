'use strict';

import CryptoUtils from "../util/crypto/Crypto";
import crypto from "node:crypto";

//import { Buffer } from 'buffer';

/**
 * Represents a Blockmess Crypto Node Transaction
 */
class BlockmessTransaction
{
	constructor(origin, dest, value)
	{
		this.origin = Buffer.from(origin, "utf-8");
		this.dest = Buffer.from(dest, "utf-8");
		this.value = value;
		this.nonce = this.getRandomInt(0, Number.MAX_SAFE_INTEGER);
		this.signature = undefined;
	}

	getRandomInt(min, max) {
		min = Math.ceil(min);
		max = Math.floor(max);
		return Math.floor(Math.random() * (max - min) + min); // The maximum is exclusive and the minimum is inclusive
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

export default BlockmessTransaction;