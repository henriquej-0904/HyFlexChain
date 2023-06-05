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
class CreateTransactionWithScWorkload extends WorkloadModuleBase {

    /**
     * Initializes the workload module instance.
     */
    constructor() {
        super();
        this.txIndex = 0;
        this.contractData =
            Buffer.from("608060405234801561001057600080fd5b506104b6806100206000396000f3fe608060405234801561001057600080fd5b506004361061002b5760003560e01c8063b61cda2814610030575b600080fd5b61004a60048036038101906100459190610274565b610060565b604051610057919061045e565b60405180910390f35b60606040518060400160405280600381526020017f706f77000000000000000000000000000000000000000000000000000000000081525090509a9950505050505050505050565b6000604051905090565b600080fd5b600080fd5b600080fd5b600080fd5b6000601f19601f8301169050919050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052604160045260246000fd5b61010f826100c6565b810181811067ffffffffffffffff8211171561012e5761012d6100d7565b5b80604052505050565b60006101416100a8565b905061014d8282610106565b919050565b600067ffffffffffffffff82111561016d5761016c6100d7565b5b610176826100c6565b9050602081019050919050565b82818337600083830152505050565b60006101a56101a084610152565b610137565b9050828152602081018484840111156101c1576101c06100c1565b5b6101cc848285610183565b509392505050565b600082601f8301126101e9576101e86100bc565b5b81356101f9848260208601610192565b91505092915050565b60008160070b9050919050565b61021881610202565b811461022357600080fd5b50565b6000813590506102358161020f565b92915050565b60008160030b9050919050565b6102518161023b565b811461025c57600080fd5b50565b60008135905061026e81610248565b92915050565b6000806000806000806000806000806101408b8d031215610298576102976100b2565b5b60008b013567ffffffffffffffff8111156102b6576102b56100b7565b5b6102c28d828e016101d4565b9a505060208b013567ffffffffffffffff8111156102e3576102e26100b7565b5b6102ef8d828e016101d4565b99505060408b013567ffffffffffffffff8111156103105761030f6100b7565b5b61031c8d828e016101d4565b98505060608b013567ffffffffffffffff81111561033d5761033c6100b7565b5b6103498d828e016101d4565b97505060808b013567ffffffffffffffff81111561036a576103696100b7565b5b6103768d828e016101d4565b96505060a06103878d828e01610226565b95505060c06103988d828e0161025f565b94505060e06103a98d828e0161025f565b9350506101006103bb8d828e01610226565b9250506101206103cd8d828e01610226565b9150509295989b9194979a5092959850565b600081519050919050565b600082825260208201905092915050565b60005b838110156104195780820151818401526020810190506103fe565b60008484015250505050565b6000610430826103df565b61043a81856103ea565b935061044a8185602086016103fb565b610453816100c6565b840191505092915050565b600060208201905081810360008301526104788184610425565b90509291505056fea2646970667358221220309541006c21160d3b53047c20a0f874641944ce737909028a2b92c2ee3dd3ae64736f6c63430008140033", "hex");
    }

    /**
     * Assemble TXs for the round.
     * @return {Promise<TxStatus[]>}
     */
    async submitTransaction() {
        const originPubKey = "EC;" + this.sutContext.encodedPublicKey;
        const destPubKey = "EC;" + this.getRandPubKey();
        const val = Util.getRandomInt32();

        const inputTxs = [HyFlexChainTransaction.createInputTx(this.getRandPubKey(), "some hash", 0)];
        const outputTxs = [HyFlexChainTransaction.createOutputTx(destPubKey, val)];
        const tx = new HyFlexChainTransaction(originPubKey, inputTxs, outputTxs);
        tx.nonce = this.txIndex;
        tx.data = this.contractData;

        this.txIndex++;

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
        return "EC;" + pubKeys[i];
    }
}

/**
 * Create a new instance of the workload module.
 * @return {WorkloadModuleInterface}
 */
function createWorkloadModule() {
    return new CreateTransactionWithScWorkload();
}

module.exports.createWorkloadModule = createWorkloadModule;
