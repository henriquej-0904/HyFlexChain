'use strict';

const { ConnectorBase, CaliperUtils, ConfigUtil, TxStatus } = require('@hyperledger/caliper-core');

const CryptoUtils = require("../util/crypto/Crypto");
const KeyPair = require("../util/crypto/KeyPair");

const Transaction = require("./HyFlexChainTransaction");
const Context = require("./Context");
const WorkerArgs = require("./WorkerArgs");

const axios = require('axios').default;
const https = require('https');

const fs = require('fs');
const path = require('path');

const Logger = CaliperUtils.getLogger('HyFlexChainConnector');

/**
 * A connector for the HyFlexChain System.
 */
class HyFlexChainConnector extends ConnectorBase {
    /**
     * Constructor
     * @param {number} workerIndex The zero-based worker index.
     */
    constructor(workerIndex) {
        super(workerIndex, "hyflexchain");

        let configPath = CaliperUtils.resolvePath(ConfigUtil.get(ConfigUtil.keys.NetworkConfig));
        let hyflexchainConfig = require(configPath).hyflexchain;

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

        if (!hyflexchainConfig.truststore_ca) {
            throw new Error(
                'No truststore CA given to securely connect to HyFlexChain nodes.'
            );
        }

        if (!hyflexchainConfig.replica_addresses) {
            throw new Error(
                'No replicas addresses given to serve as destination in transactions.'
            );
        }

        if (!hyflexchainConfig.smart_contracts) {
            throw new Error(
                'No smart contracts are defined.'
            );
        }
    }

    async init(workerInit) {
        // this._throwNotImplemented('init');

        const smart_contracts_files = new Map(Object.entries(this.hyflexchainConfig.smart_contracts));
        this.smart_contracts_map = new Map();

        smart_contracts_files.forEach((element, k) => {
            this.smart_contracts_map.set(k,
                Buffer.from(fs.readFileSync(path.resolve(element), { encoding : "utf-8" }), "hex")
            )
        });

        if (workerInit || !this.hyflexchainConfig.reference_smart_contract)
            return;

        const httpsAgent = new https.Agent({
            rejectUnauthorized: false, // (NOTE: this will disable client verification)
            ca: fs.readFileSync(this.hyflexchainConfig.truststore_ca)
        })

        const httpClient = axios.create(
            {
                baseURL: this.hyflexchainConfig.url[0],
                timeout: this.hyflexchainConfig.connection_timeout,
                httpsAgent: httpsAgent
                //headers: {'X-Custom-Header': 'foobar'}
            }
        );

        const onFailure = (err) => {
            Logger.error(`Failed to install smart contract.`);
            Logger.error(err);
        };

        this.installed_smart_contracts = new Map();

        for (const iterator of this.smart_contracts_map) {
            let k = iterator[0];
            let element = iterator[1];
            
            await httpClient.post("/hyflexchain/scmi/install", element,
            {
                headers: {
                    "Content-Type": "application/octet-stream"
                }
            }).then(response => {
                if (response.status == 200) {
                    Logger.error(`Installed smart contract: ` + k + ", " + response.data);
                    this.installed_smart_contracts.set(k, response.data);
                }
                else {
                    onFailure(response.statusText);
                }
            }).catch(reason => {
                onFailure(reason);
            });
        }
        
        Logger.error("installed contracts: " + JSON.stringify(Object.fromEntries(this.installed_smart_contracts)));
    }

    async installSmartContract() {
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

        for (let i = 0; i < number; i++) {
            keyPairs[i] = this.cryptoUtils.genKeyPairEC();
        }

        const encodedWorkersKeys = keyPairs.map(
            keyPair => this.cryptoUtils.encodeKeyPair(keyPair)
        );

        const replica_addresses_file_path =
            path.resolve(this.hyflexchainConfig.replica_addresses);

        Logger.error("addresses.json path: " + replica_addresses_file_path);

        const destReplicasAddresses = Object.keys(
            require(replica_addresses_file_path)
        );

        let workersArgs = [];

        for (let i = 0; i < number; i++) {
            workersArgs[i] = new WorkerArgs(
                this.hyflexchainConfig.url[i],
                encodedWorkersKeys[i],
                destReplicasAddresses,
                Object.fromEntries(this.installed_smart_contracts)
            );
        }

        return workersArgs;
    }

    /**
     * Return the Worker context associated with the given callback module name.
     * Creates a client instance to the specified replica.
     * @param {Number} roundIndex The zero-based round index of the test.
     * @param {object} args worker arguments.
     * @return {Context} The assembled Worker context.
     * @async
     */
    async getContext(roundIndex, args) {
        let context = new Context(WorkerArgs.fromArgs(args), this.workerIndex, this.cryptoUtils);
        this.context = context;

        // connect to replica

        const httpsAgent = new https.Agent({
            rejectUnauthorized: false, // (NOTE: this will disable client verification)
            ca: fs.readFileSync(this.hyflexchainConfig.truststore_ca)
        })

        this.httpClient = axios.create(
            {
                baseURL: context.getUrl(),
                timeout: this.hyflexchainConfig.connection_timeout,
                httpsAgent: httpsAgent
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
    // 	async _sendSingleRequest(request)
    // 	{
    //         request.sign(this.context.getKeyPair().getPrivateKey(), this.cryptoUtils);

    //         const data = {
    //             version : request.version,
    //             sender : request.sender,
    //             signatureType : request.signatureType,
    //             signature : request.signature,
    //             nonce : request.nonce,
    //             transactionType : request.transactionType,
    //             smartContract : request.smartContract,
    //             inputTxs : request.inputTxs,
    //             outputTxs : request.outputTxs,
    //             data : request.data
    //         }

    //         // Logger.error(JSON.stringify(data));

    //         let status = new TxStatus();

    //         const onFailure = (err) => {
    //             status.SetStatusFail();
    //             Logger.error(`Failed tx.`);
    //             Logger.error(err);
    //         };

    //         const onSuccess = (reply) => {
    //             status.SetID(reply);
    //             status.SetResult(reply);
    //             status.SetVerification(true);
    //             status.SetStatusSuccess();
    //         };

    //         return this.httpClient.post("/hyflexchain/ti/transaction-json", data,
    //         {
    //             // headers : {
    //             //     "Content-Type" : "application/json"
    //             // }
    //             headers : {
    //                 "Content-Type" : "application/json"
    //             }
    //         }).then(response => {
    //             if (response.status == 200)
    //             {
    //                 onSuccess(response.data);
    //                 return status;
    //             }
    //             else
    //             {
    //                 onFailure(response.statusText);
    //                 return status;
    //             }
    //         }).catch(reason => {
    //             onFailure(reason);
    //             return status;
    //         });
    // 	}
    // }

    /**
    * Submit a transaction to the blockmess blockchain.
    * @param {Transaction} request Methods call data.
    * @return {Promise<TxStatus>} Result and stats of the transaction invocation.
    */
    async _sendSingleRequest(request) {
        request.sign(this.context.getKeyPair().getPrivateKey(), this.cryptoUtils);

        const data = request.toJson();

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

        return this.httpClient.post("/hyflexchain/ti/transaction-json", data,
            {
                headers: {
                    "Content-Type": "application/json"
                }
            }).then(response => {
                if (response.status == 200) {
                    onSuccess(response.data);
                    return status;
                }
                else {
                    onFailure(this.context.getUrl() + "\n" + response.statusText);
                    return status;
                }
            }).catch(reason => {
                onFailure(this.context.getUrl() + "\n" + reason);
                return status;
            });
    }
}

module.exports = HyFlexChainConnector;