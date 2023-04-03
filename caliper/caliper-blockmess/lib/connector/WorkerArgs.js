'use strict';

/**
 * A wrapper for worker args.
 */
class WorkerArgs
{
    /**
     * Create a new Worker Args
     * @param {string} url the url of the replica to connect to
     * @param {string[]} keyPair the key pair of this worker
     * @param {string[]} publicKeys the public keys of all workers
     */
    constructor(url, keyPair, publicKeys)
    {
        this.url = url;
        this.keyPair = keyPair;
        this.publicKeys = publicKeys;
    }

    /**
     * @returns {string} the url of the replica to connect to
     */
    getUrl()
    {
        return this.url;
    }

    /**
     * @returns {string[]} the key pair of this worker
     */
    getKeyPair()
    {
        return this.keyPair;
    }

    /**
     * @returns {string[]} the public keys of all workers
     */
    getPublicKeys()
    {
        return this.publicKeys;
    }
}

module.exports = WorkerArgs;