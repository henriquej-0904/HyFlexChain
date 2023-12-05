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

const HyFlexChainTransaction = require("../connector/HyFlexChainTransaction");

const Context = require("../connector/Context");

const Util = require('../util/Util');

const Buffer = require('buffer').Buffer;

/**
 * Workload module for the benchmark round.
 */
class CreateTransactionPoW_BftsmartWorkload extends WorkloadModuleBase {

    /**
     * Initializes the workload module instance.
     */
    constructor() {
        super();
        this.txIndex = 0;
        this.next = 0;
    }
    
    /**
     * Assemble TXs for the round.
     * @return {Promise<TxStatus[]>}
     */
    async submitTransaction() {

        if (!this.smartContracts)
        {
            this.smartContracts = [
                this.getSmartContract("pow"),
                this.getSmartContract("bftsmart")
            ];
        }

        const originPubKey = Buffer.from("01" + this.sutContext.encodedPublicKey, 'hex');
        const destAddress = this.getRandDestAddress();
        const val = Util.getRandomInt32();

        const inputTxs = [HyFlexChainTransaction.createInputTx(Buffer.from("some hash", "utf-8"), 0)];
        const outputTxs = [HyFlexChainTransaction.createOutputTx(destAddress, val)];
        const tx = new HyFlexChainTransaction(HyFlexChainTransaction.TRANSFER, originPubKey, inputTxs, outputTxs);
        tx.nonce = this.txIndex;
        tx.smartContract = this.smartContracts[this.next];

        this.txIndex++;
        this.next = (this.next + 1) % 2;

        return this.sutAdapter.sendRequests(tx);
    }

    /**
     * Get a rand replica address from the array of destination addresses
     * @return {Buffer} random replica address
     */
    getRandDestAddress()
    {
        const destAddresses = this.sutContext.destAddresses;
        const i = Util.getRandomInt(0, destAddresses.length);
        return destAddresses[i];
    }

    getSmartContract(consensus)
    {
        // reference smart contract
        if (this.sutAdapter.hyflexchainConfig.reference_smart_contract)
        {
            const ref = this.sutContext.installedContracts.get(consensus);
            return HyFlexChainTransaction.smartContractRef(ref);
        } else // pyggyback smart contract
        {
            const contractData = this.sutAdapter.smart_contracts_map.get(consensus);
            return HyFlexChainTransaction.smartContractCode(contractData);
        }
    }
}

/**
 * Create a new instance of the workload module.
 * @return {WorkloadModuleInterface}
 */
function createWorkloadModule() {
    return new CreateTransactionPoW_BftsmartWorkload();
}

module.exports.createWorkloadModule = createWorkloadModule;
