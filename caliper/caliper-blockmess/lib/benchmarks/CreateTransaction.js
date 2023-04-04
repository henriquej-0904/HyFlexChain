/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

'use strict';

const WorkloadModuleBase = require('@hyperledger/caliper-core').WorkloadModuleBase;

const BlockmessTransaction = require("../connector/BlockmessTransaction");

const Context = require("../connector/Context");

const Util = require('../util/Util');

/**
 * Workload module for the benchmark round.
 */
class CreateTransactionWorkload extends WorkloadModuleBase {
    /**
     * Initializes the workload module instance.
     */
    constructor() {
        super();
        this.txIndex = 0;
    }

    /**
     * Assemble TXs for the round.
     * @return {Promise<TxStatus[]>}
     */
    async submitTransaction() {
        this.txIndex++;

        const originPubKey = this.sutContext.encodedPublicKey;
        const destPubKey = this.getRandPubKey();
        const val = Util.getRandomInt32();

        const tx = new BlockmessTransaction(originPubKey, destPubKey, val);

        return this.sutAdapter.sendRequests(tx);
    }

    /**
     * Get a rand pub key from the array of public keys
     * @return {string} random public key
     */
    getRandPubKey()
    {
        
        const pubKeys = this.sutContext.encodedPublicKeys;
        const i = Util.getRandomIntExcept(0, pubKeys.length, this.workerIndex);
        return pubKeys[i];
    }
}

/**
 * Create a new instance of the workload module.
 * @return {WorkloadModuleInterface}
 */
function createWorkloadModule() {
    return new CreateTransactionWorkload();
}

module.exports.createWorkloadModule = createWorkloadModule;
