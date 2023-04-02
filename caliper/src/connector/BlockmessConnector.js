'use strict';

const {ConnectorBase, CaliperUtils, ConfigUtil, TxStatus} = require('@hyperledger/caliper-core');

const Crypto = require("../util/crypto/Crypto").default;
const KeyPair = require("../util/crypto/KeyPair").default;

const Transaction = require("./BlockmessTransaction").default;

import axios from 'axios';

/**
 * A connector for the Blockmess System.
 */
class BlockmessConnector extends ConnectorBase
{
	/**
     * Constructor
     * @param {number} workerIndex The zero-based worker index.
     */
    constructor(workerIndex) {
        super(workerIndex, "blockmess");

        let configPath = CaliperUtils.resolvePath(ConfigUtil.get(ConfigUtil.keys.NetworkConfig));
        let blockmessConfig = require(configPath).blockmess;

        // throws on configuration error
        this.checkConfig(workerIndex, blockmessConfig);

        this.blockmessConfig = blockmessConfig;

        this.workerIndex = workerIndex;

        this.httpClient = undefined;
        this.context = undefined;

        this.crypto = new Crypto();
    }

    /**
     * Check the blockmess networkconfig file for errors, throw if invalid
     * @param {object} blockmessConfig The blockmess networkconfig to check.
     */
    checkConfig(workerIndex, blockmessConfig) {

        let url = workerIndex < 0 ? blockmessConfig.url[0] : blockmessConfig.url[workerIndex];

        if (!url) {
            throw new Error(
                'No URL given to access the Blockmess Crypto Node SUT. Please check your network configuration. '
            );
        }

        if (url.toLowerCase().indexOf('http') === -1) {
            throw new Error(
                'Blockmess Crypto Node benchmarks must use http(s) RPC connections, since it is the only supported' +
                'communication protocol so far.'
            );
        }

        if (!blockmessConfig.blockmess_config_folder) {
            throw new Error(
                'No Blockmess config folder was given. Please check your network configuration. '
            );
        }
    }

    async init(workerInit) {
        // this._throwNotImplemented('init');
    }

    async installSmartContract()
    {
        // blockmess does not support smart contracts.
    }

    /**
     * Generate key pairs for all workers and
     * prepare args.
     * @param {Number} number of workers to prepare
     * @returns {WorkerArgs[]} worker args
     * @async
     */
    async prepareWorkerArguments(number) {
        let keyPairs = [];
        
        for (let i = 0 ; i < number ; i++) {
            keyPairs[i] = this.crypto.genKeyPairEC();
        }

        let encodedKeyPairs = keyPairs.map(
            keyPair => this.crypto.encodeKeyPair(keyPair)
        );

        let encodedPublicKeys = encodedKeyPairs.map(
            pair => pair[0]
        );

        let workersArgs = [];

        for (let i = 0 ; i < number ; i++) {
            workersArgs[i] = new WorkerArgs(
                this.blockmessConfig.url[i],
                encodedKeyPairs[i],
                encodedPublicKeys
            );
        }
        return workersArgs;
    }

    /**
     * Return the Blockmess context associated with the given callback module name.
     * Creates a client instance to the specified replica.
     * @param {Number} roundIndex The zero-based round index of the test.
     * @param {WorkerArgs} args worker arguments.
     * @return {object} The assembled Blockmess context.
     * @async
     */
    async getContext(roundIndex, args) {
        let context = new Context(args, this.workerIndex, this.crypto);
        this.context = context;

        // connect to replica

        this.httpClient = axios.create(
            {
                baseURL: context.getUrl(),
                timeout: this.blockmessConfig.connection_timeout,
                //headers: {'X-Custom-Header': 'foobar'}
            }
        );
        
        return context;
    }

    /**
     * Release the given Ethereum context.
     * @async
     */
    async releaseContext() {
        this.context = undefined;
        this.httpClient = undefined;
    }

    /**
     * Submit a transaction to the blockmess blockchain.
     * @param {Transaction} request Methods call data.
     * @return {Promise<TxStatus>} Result and stats of the transaction invocation.
     */
	async _sendSingleRequest(request)
	{
		//TODO: sign transaction & send request to blockmess
		request.sign(this.context.getKeyPair().getPrivateKey(), this.crypto);

        let status = new TxStatus();

        const onFailure = (err) => {
            status.SetStatusFail();
            logger.error(`Failed tx.`);
            logger.error(err);
        };

        const onSuccess = (reply) => {
            status.SetID(reply);
            status.SetResult(reply);
            status.SetVerification(true);
            status.SetStatusSuccess();
        };

        return this.httpClient.post("/crypto-node/transaction", request,
        {
            headers : {
                "Content-Type" : "application/json"
            }
        }).then(response => {
            if (response.status == 200)
            {
                onSuccess(response.data);
                return status;
            }
            else
            {
                onFailure(response.statusText);
                return status;
            }
        }).catch(reason => {
            onFailure(reason);
            return status;
        });
	}

	
}

/**
 * A wrapper for worker context.
 */
class Context
{
    /**
     * Create a context for the worker
     * @param {WorkerArgs} workerArgs
     * @param {Number} workerIndex
     * @param {Crypto} crypto 
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

module.exports = BlockmessConnector;