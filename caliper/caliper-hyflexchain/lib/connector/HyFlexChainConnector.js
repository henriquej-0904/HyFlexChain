'use strict';

const {ConnectorBase, CaliperUtils, ConfigUtil, TxStatus} = require('@hyperledger/caliper-core');

const CryptoUtils = require("../util/crypto/Crypto");
const KeyPair = require("../util/crypto/KeyPair");

const Transaction = require("./HyFlexChainTransaction");
const Context = require("./Context");
const WorkerArgs = require("./WorkerArgs");

const axios = require('axios').default;

const Logger = CaliperUtils.getLogger('HyFlexChainConnector');

/**
 * A connector for the HyFlexChain System.
 */
class HyFlexChainConnector extends ConnectorBase
{
	/**
     * Constructor
     * @param {number} workerIndex The zero-based worker index.
     */
    constructor(workerIndex) {
        super(workerIndex, "hyflexchain");

        let configPath = CaliperUtils.resolvePath(ConfigUtil.get(ConfigUtil.keys.NetworkConfig));
        let hyflexchainConfig = require(configPath).blockmess;

        // throws on configuration error
        this.checkConfig(workerIndex, hyflexchainConfig);

        this.hyflexchainConfig = hyflexchainConfig;

        this.workerIndex = workerIndex;

        this.httpClient = undefined;
        this.context = undefined;

        this.cryptoUtils = new CryptoUtils();
    }

    /**
     * Check the blockmess networkconfig file for errors, throw if invalid
     * @param {object} hyflexchainConfig The blockmess networkconfig to check.
     */
    checkConfig(workerIndex, hyflexchainConfig) {

        let url = workerIndex < 0 ? hyflexchainConfig.url[0] : hyflexchainConfig.url[workerIndex];

        if (!url) {
            throw new Error(
                'No URL given to access the HyFlexChain Node SUT. Please check your network configuration. '
            );
        }

        if (url.toLowerCase().indexOf('http') === -1) {
            throw new Error(
                'HyFlexChain Node benchmarks must use http(s) RPC connections, since it is the only supported' +
                'communication protocol so far.'
            );
        }

        /* if (!blockmessConfig.blockmess_config_folder) {
            throw new Error(
                'No Blockmess config folder was given. Please check your network configuration. '
            );
        } */
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
     * @returns {Promise<object[]>} worker args
     * @async
     */
    async prepareWorkerArguments(number) {
        let keyPairs = [];
        
        for (let i = 0 ; i < number ; i++) {
            keyPairs[i] = this.cryptoUtils.genKeyPairEC();
        }

        let encodedKeyPairs = keyPairs.map(
            keyPair => this.cryptoUtils.encodeKeyPair(keyPair)
        );

        let encodedPublicKeys = encodedKeyPairs.map(
            pair => pair[0]
        );

        let workersArgs = [];

        for (let i = 0 ; i < number ; i++) {
            workersArgs[i] = new WorkerArgs(
                this.hyflexchainConfig.url[i],
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
     * @param {object} args worker arguments.
     * @return {Context} The assembled Blockmess context.
     * @async
     */
    async getContext(roundIndex, args) {
        let context = new Context(WorkerArgs.fromArgs(args), this.workerIndex, this.cryptoUtils);
        this.context = context;

        // connect to replica

        this.httpClient = axios.create(
            {
                baseURL: context.getUrl(),
                timeout: this.hyflexchainConfig.connection_timeout,
                //headers: {'X-Custom-Header': 'foobar'}
            }
        );
        
        return context;
    }

    /**
     * Release the given Blockmess context.
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
        request.sign(this.context.getKeyPair().getPrivateKey(), this.cryptoUtils);
        request.performHash(this.cryptoUtils);

        const data = {
            version : request.version,
            hash : request.hash,
            sender : request.sender,
            signatureType : request.signatureType,
            signature : request.signature,
            nonce : request.nonce,
            inputTxs : request.inputTxs,
            outputTxs : request.outputTxs,
            data : request.data.toString("base64")
        }

        // Logger.error(JSON.stringify(data));

        let status = new TxStatus();

        const onFailure = (err) => {
            status.SetStatusFail();
            Logger.error(`Failed tx.`);
            Logger.error(err);
        };

        const onSuccess = (reply) => {
            status.SetID(reply);
            status.SetResult(reply);
            status.SetVerification(true);
            status.SetStatusSuccess();
        };

        return this.httpClient.post("/hyflexchain/ti/transaction", data,
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

module.exports = HyFlexChainConnector;