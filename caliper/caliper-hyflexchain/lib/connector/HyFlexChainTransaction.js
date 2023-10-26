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
	static TRANSFER = ["TRANSFER", Buffer.from([1])];

	/**
	 * Contract deployment transactions: a transaction that creates
	 * and installs a smart contract on the chain. After being installed
	 * it can be referenced in future transactions for execution.
	 */
	static CONTRACT_CREATE = ["CONTRACT_CREATE", Buffer.from([2])];

	/**
	 * Revoke a contract previously installed on the chain.
	 * After this transaction is approved/executed it is no longer
	 * possible to reference and execute the revoked smart contract.
	 */
	static CONTRACT_REVOKE = ["CONTRACT_REVOKE", Buffer.from([3])];

	/**
	 * An internal type used by HyFlexChain nodes to propose
	 * committees for the future.
	 */
	static COMMITTEE_ELECTION = ["COMMITTEE_ELECTION", Buffer.from([4])];

	/**
	 * An internal type used by HyFlexChain nodes to rotate
	 * the currently executing committee.
	 */
	static COMMITTEE_ROTATION = ["COMMITTEE_ROTATION", Buffer.from([5])];

	
	// Signature type
	
	static SHA256withECDSA = "SHA256withECDSA";


	

	/**
	 * Create a new transaction
	 * @param {(string | Buffer)[]} txType
	 * @param {Buffer} origin 
	 * @param {object[]} inputTxs 
	 * @param {object[]} outputTxs 
	 */
	constructor(txType, origin, inputTxs, outputTxs)
	{
		this.version = "V1_0";
		this.sender = {address : origin};
		this.signatureType = HyFlexChainTransaction.SHA256withECDSA;
		this.signature = undefined;
		this.nonce = Util.getRandomInt(0, Number.MAX_SAFE_INTEGER);
		this.transactionType = txType;
		this.smartContract = undefined;
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
		sig.update(this.sender.address);

		let buff = Buffer.alloc(8);
		buff.writeBigInt64BE(BigInt(this.nonce));
		sig.update(buff);

		sig.update(this.transactionType[1]);
		
		sig.update(this.smartContract.id.address);
		sig.update(this.smartContract.code);

		this.inputTxs.forEach((element, index, array) => {
			sig.update(element.txId);
			
			let buff = Buffer.alloc(4);
			buff.writeInt32BE(element.outputIndex);
			sig.update(buff);
		});

		this.outputTxs.forEach((element, index, array) => {
			sig.update(element.recipient.address);

			let buff = Buffer.alloc(8);
			buff.writeBigInt64BE(BigInt(element.value));
			sig.update(buff);
		});

		sig.update(this.data);

		sig.end();

		this.signature = sig.sign(privKey);
	}

	toJson()
	{
		return {
            version : this.version,
            sender : {address : this.sender.address.toString("base64")},
            signatureType : this.signatureType,
            signature : this.signature.toString("base64"),
            nonce : this.nonce,
            transactionType : this.transactionType[0],
            smartContract : HyFlexChainTransaction.toJsonSmartContract(this.smartContract),
            inputTxs : this.inputTxs.map(v => HyFlexChainTransaction.toJsonInputTx(v)),
            outputTxs : this.outputTxs.map(v => HyFlexChainTransaction.toJsonOutputTx(v)),
            data : this.data.toString("base64")
        }
	}

	static smartContract(ref, code)
	{
		return {id : {address : ref}, code : code};
	}

	static smartContractRef(ref)
	{
		return HyFlexChainTransaction.smartContract(ref, Buffer.alloc(0));
	}

	static smartContractCode(code)
	{
		return HyFlexChainTransaction.smartContract(Buffer.alloc(0), code);
	}

	static toJsonSmartContract(contract)
	{
		return {id : {address : contract.id.address.toString("base64")}, code : contract.code.toString("base64")};
	}

	static toJsonInputTx(inputTx)
	{
		return {
			txId : inputTx.txId.toString("base64"),
			outputIndex : inputTx.outputIndex
		};
	}

	static toJsonOutputTx(outputTx)
	{
		return {recipient : {address : outputTx.recipient.address.toString("base64")}, value : outputTx.value};
	}

	static createInputTx(txHash, outputIndex)
	{
		return {
			txId : txHash,
			outputIndex : outputIndex
		};
	}

	static createOutputTx(address, value)
	{
		return {recipient : {address : address}, value : value};
	}
	
}

module.exports = HyFlexChainTransaction;