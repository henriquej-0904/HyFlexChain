'use strict';

const CryptoUtils = require("../util/crypto/Crypto");
const KeyPair = require("../util/crypto/KeyPair");
const WorkerArgs = require("./WorkerArgs");

/**
 * A wrapper for worker context.
 */
class Context
{
    /**
     * Create a context for the worker
     * @param {WorkerArgs} workerArgs
     * @param {Number} workerIndex
     * @param {CryptoUtils} crypto 
     */
    constructor(workerArgs, workerIndex, crypto)
    {
        this.url = workerArgs.getUrl();

        const encodedWorkerKeyPair = workerArgs.getKeyPair();
        this.encodedPublicKey = encodedWorkerKeyPair[0];
        this.keyPair = crypto.decodeKeyPair(encodedWorkerKeyPair);

        this.destAddresses = workerArgs.getDestAddresses()
            .map(a => Buffer.from(a.substring(2), "hex"));

        const installedContracts = new Map(Object.entries(workerArgs.getInstalledContracts()));
        // console.log(installedContracts);
        this.installedContracts = new Map();
        installedContracts.forEach((v, k) => {
            this.installedContracts.set(k, Buffer.from(v.substring(2), "hex"));
        });

        // console.log(Object.fromEntries(this.installedContracts));
    }

    /**
     * @returns {string} the url of the replica to connect to
     */
    getUrl()
    {
        return this.url;
    }

    /**
     * @returns {KeyPair} the key pair of this worker
     */
    getKeyPair()
    {
        return this.keyPair;
    }

    /**
     * @returns {string} the encoded public key of this worker
     */
    getEncodedPublicKey()
    {
        return this.encodedPublicKey;
    }

    /**
     * @returns {Buffer[]} the destination addresses for generated transactions
     */
    getDestAddresses()
    {
        return this.destAddresses;
    }

    getInstalledContracts()
    {
        return this.installedContracts;
    }
}

module.exports = Context;