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
        this.keyPair = crypto.decodeKeyPair(workerArgs.getKeyPair());
        this.encodedPublicKeys = workerArgs.getPublicKeys();
        this.encodedPublicKey = this.encodedPublicKeys[workerIndex];
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
     * @returns {string[]} the encoded public keys of all workers
     */
    getEncodedPublicKeys()
    {
        return this.publicKeys;
    }
}

module.exports = Context;