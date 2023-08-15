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
class CreateTransactionPowWorkload extends WorkloadModuleBase {

    /**
     * Initializes the workload module instance.
     */
    constructor() {
        super();
        this.txIndex = 0;
        this.contractData =
            Buffer.from("608060405234801561001057600080fd5b50610708806100206000396000f3fe608060405234801561001057600080fd5b506004361061002b5760003560e01c80639e69865614610030575b600080fd5b61004a6004803603810190610045919061045d565b610060565b60405161005791906105c7565b60405180910390f35b606060006040518060400160405280601281526020017f22636f6e73656e737573223a2022706f77220000000000000000000000000000815250905060006040518060400160405280601181526020017f2262617463684d6574726963223a203130000000000000000000000000000000815250905060006040518060600160405280602c81526020016106a7602c91399050826040516020016101049190610671565b604051602081830303815290604052935050505095945050505050565b6000604051905090565b600080fd5b600080fd5b600080fd5b600080fd5b6000601f19601f8301169050919050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052604160045260246000fd5b6101888261013f565b810181811067ffffffffffffffff821117156101a7576101a6610150565b5b80604052505050565b60006101ba610121565b90506101c6828261017f565b919050565b600067ffffffffffffffff8211156101e6576101e5610150565b5b6101ef8261013f565b9050602081019050919050565b82818337600083830152505050565b600061021e610219846101cb565b6101b0565b90508281526020810184848401111561023a5761023961013a565b5b6102458482856101fc565b509392505050565b600082601f83011261026257610261610135565b5b813561027284826020860161020b565b91505092915050565b600067ffffffffffffffff82111561029657610295610150565b5b602082029050602081019050919050565b600080fd5b60008160070b9050919050565b6102c2816102ac565b81146102cd57600080fd5b50565b6000813590506102df816102b9565b92915050565b60006102f86102f38461027b565b6101b0565b9050808382526020820190506020840283018581111561031b5761031a6102a7565b5b835b81811015610344578061033088826102d0565b84526020840193505060208101905061031d565b5050509392505050565b600082601f83011261036357610362610135565b5b81356103738482602086016102e5565b91505092915050565b600067ffffffffffffffff82111561039757610396610150565b5b602082029050602081019050919050565b60006103bb6103b68461037c565b6101b0565b905080838252602082019050602084028301858111156103de576103dd6102a7565b5b835b8181101561042557803567ffffffffffffffff81111561040357610402610135565b5b808601610410898261024d565b855260208501945050506020810190506103e0565b5050509392505050565b600082601f83011261044457610443610135565b5b81356104548482602086016103a8565b91505092915050565b600080600080600060a086880312156104795761047861012b565b5b600086013567ffffffffffffffff81111561049757610496610130565b5b6104a38882890161024d565b955050602086013567ffffffffffffffff8111156104c4576104c3610130565b5b6104d08882890161024d565b945050604086013567ffffffffffffffff8111156104f1576104f0610130565b5b6104fd8882890161034e565b935050606086013567ffffffffffffffff81111561051e5761051d610130565b5b61052a8882890161042f565b925050608061053b888289016102d0565b9150509295509295909350565b600081519050919050565b600082825260208201905092915050565b60005b83811015610582578082015181840152602081019050610567565b60008484015250505050565b600061059982610548565b6105a38185610553565b93506105b3818560208601610564565b6105bc8161013f565b840191505092915050565b600060208201905081810360008301526105e1818461058e565b905092915050565b7f7b00000000000000000000000000000000000000000000000000000000000000815250565b600081905092915050565b600061062582610548565b61062f818561060f565b935061063f818560208601610564565b80840191505092915050565b7f7d00000000000000000000000000000000000000000000000000000000000000815250565b600061067c826105e9565b60018201915061068c828461061a565b91506106978261064b565b6001820191508190509291505056fe227369676e617475726554797065223a20225348413235367769746845434453412d73656370353231723122a26469706673582212201159e01ecd0c155835a8798d2c44beb979a60ffd7a7da3fa42f644567c5e16a364736f6c63430008140033", "hex");
    }

    /**
     * Assemble TXs for the round.
     * @return {Promise<TxStatus[]>}
     */
    async submitTransaction() {
        const originPubKey = Buffer.from("01" + this.sutContext.encodedPublicKey, 'hex');
        const destAddress = this.getRandDestAddress();
        const val = Util.getRandomInt32();

        const inputTxs = [HyFlexChainTransaction.createInputTx(Buffer.from("some hash", "utf-8"), 0)];
        const outputTxs = [HyFlexChainTransaction.createOutputTx(destAddress, val)];
        const tx = new HyFlexChainTransaction(HyFlexChainTransaction.TRANSFER, originPubKey, inputTxs, outputTxs);
        tx.nonce = this.txIndex;
        tx.smartContract = HyFlexChainTransaction.smartContractCode(this.contractData);

        this.txIndex++;

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
}

/**
 * Create a new instance of the workload module.
 * @return {WorkloadModuleInterface}
 */
function createWorkloadModule() {
    return new CreateTransactionPowWorkload();
}

module.exports.createWorkloadModule = createWorkloadModule;
