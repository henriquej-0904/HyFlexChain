'use strict';

const CryptoUtils = require( "../util/crypto/Crypto");
const crypto = require("node:crypto");

const Util = require('../util/Util');

const Buffer = require('buffer').Buffer;

/**
 * Represents a HyFlexChain Node Transaction
 */
class HyFlexChainTransaction
{
	static TRANSFER = "TRANSFER";

	/**
	 * Contract deployment transactions: a transaction that creates
	 * and installs a smart contract on the chain. After being installed
	 * it can be referenced in future transactions for execution.
	 */
	static CONTRACT_CREATE = "CONTRACT_CREATE";

	/**
	 * Revoke a contract previously installed on the chain.
	 * After this transaction is approved/executed it is no longer
	 * possible to reference and execute the revoked smart contract.
	 */
	static CONTRACT_REVOKE = "CONTRACT_REVOKE";

	/**
	 * An internal type used by HyFlexChain nodes to propose
	 * committees for the future.
	 */
	static COMMITTEE_ELECTION = "COMMITTEE_ELECTION";

	/**
	 * An internal type used by HyFlexChain nodes to rotate
	 * the currently executing committee.
	 */
	static COMMITTEE_ROTATION = "COMMITTEE_ROTATION";

	/**
	 * Create a new transaction
	 * @param {string} txType
	 * @param {string} origin 
	 * @param {object[]} inputTxs 
	 * @param {object[]} outputTxs 
	 */
	constructor(txType, origin, inputTxs, outputTxs)
	{
		this.version = "V1_0";
		this.hash = undefined;
		this.transactionType = txType;
		this.sender = {address : origin};
		this.signatureType = "SHA256withECDSA";
		this.signature = undefined;
		this.nonce = Util.getRandomInt(0, Number.MAX_SAFE_INTEGER);
		this.inputTxs = inputTxs;
		this.outputTxs = outputTxs;
		this.data = Buffer.alloc(1);
	}


	/**
	 * Sign transaction
	 * @param {crypto.KeyObject} privKey 
	 * @param {CryptoUtils} cryptoUtils 
	 */
	sign(privKey, cryptoUtils)
	{
		let sig = cryptoUtils.getSigInstance();

		sig.update(Buffer.from(this.version, "utf-8"));
		sig.update(Buffer.from(this.transactionType, "utf-8"));
		sig.update(Buffer.from(this.sender.address, "utf-8"));

		let buff = Buffer.alloc(8);
		buff.writeBigInt64BE(BigInt(this.nonce));
		sig.update(buff);

		this.inputTxs.forEach((element, index, array) => {
			sig.update(Buffer.from(element.txId.senderAddress.address, "utf-8"));
			sig.update(Buffer.from(element.txId.txHash, "utf-8"));
			
			let buff = Buffer.alloc(4);
			buff.writeInt32BE(element.outputIndex);
			sig.update(buff);
		});

		this.outputTxs.forEach((element, index, array) => {
			sig.update(Buffer.from(element.recipient.address, "utf-8"));

			let buff = Buffer.alloc(8);
			buff.writeBigInt64BE(BigInt(element.value));
			sig.update(buff);
		});

		sig.update(this.data);

		sig.end();

		this.signature = sig.sign(privKey).toString("hex");
	}

	/**
	 * Perform a hash on the transaction.
	 * 
	 * @param {CryptoUtils} cryptoUtils
	 */
	performHash(cryptoUtils)
	{
		let hash = cryptoUtils.getHashInstance();

		hash.update(Buffer.from(this.version, "utf-8"));
		hash.update(Buffer.from(this.transactionType, "utf-8"));
		hash.update(Buffer.from(this.sender.address, "utf-8"));
		hash.update(Buffer.from(this.signatureType, "utf-8"));
		hash.update(Buffer.from(this.signature, "utf-8"));

		let buff = Buffer.alloc(8);
		buff.writeBigInt64BE(BigInt(this.nonce));
		hash.update(buff);

		this.inputTxs.forEach((element, index, array) => {
			hash.update(Buffer.from(element.txId.senderAddress.address, "utf-8"));
			hash.update(Buffer.from(element.txId.txHash, "utf-8"));
			
			let buff = Buffer.alloc(4);
			buff.writeInt32BE(element.outputIndex);
			hash.update(buff);
		});

		this.outputTxs.forEach((element, index, array) => {
			hash.update(Buffer.from(element.recipient.address, "utf-8"));

			let buff = Buffer.alloc(8);
			buff.writeBigInt64BE(BigInt(element.value));
			hash.update(buff);
		});

		hash.update(this.data);

		hash.end();

		this.hash = hash.digest("hex");
	}

	static createInputTx(senderAddress, txHash, outputIndex)
	{
		return {
			txId : {senderAddress : {address : senderAddress}, txHash : txHash},
			outputIndex : outputIndex
		};
	}

	static createOutputTx(address, value)
	{
		return {recipient : {address : address}, value : value};
	}
	
}

module.exports = HyFlexChainTransaction;