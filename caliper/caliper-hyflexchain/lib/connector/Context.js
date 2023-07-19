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

        this.destAddresses = workerArgs.getDestAddresses();
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
     * @returns {string[]} the destination addresses for generated transactions
     */
    getDestAddresses()
    {
        return this.destAddresses;
    }
}

module.exports = Context;