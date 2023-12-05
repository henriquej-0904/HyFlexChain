'use strict';

/**
 * A wrapper for worker args.
 */
class WorkerArgs
{
    /**
     * Construct args from an object with the required fields.
     * @param {object} obj 
     */
    static fromArgs(obj)
    {
        return new WorkerArgs(obj.url, obj.keyPair, obj.destAddresses, obj.installedContracts);
    }

    /**
     * Create a new Worker Args
     * @param {string} url the url of the replica to connect to
     * @param {string[]} keyPair the key pair of this worker
     * @param {string[]} destAddresses the destination addresses for generated transactions
     * @param installedContracts the installed smart contracts
     */
    constructor(url, keyPair, destAddresses, installedContracts)
    {
        this.url = url;
        this.keyPair = keyPair;
        this.destAddresses = destAddresses;
        this.installedContracts = installedContracts;
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
     * @returns {string[]} the destination addresses for generated transactions
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

module.exports = WorkerArgs;