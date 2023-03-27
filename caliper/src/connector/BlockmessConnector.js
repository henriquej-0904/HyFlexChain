'use strict';

const ConnectorBase = require("@hyperledger/caliper-core/lib/common/core/connector-base");

/**
 * A connector for the Blockmess Blockchain System.
 */
class BlockmessConnector extends ConnectorBase
{
	/**
     * Constructor
     * @param {number} workerIndex The zero-based worker index.
     */
    constructor(workerIndex) {
        super(workerIndex, "blockmess-connector");
    }

	async _sendSingleRequest(request)
	{
		//TODO: send requests to blockmess
		throw new Error('Method "_sendSingleRequest" is not implemented for Blockmess Connector.');
	}

	async init(workerInit) {
        this._throwNotImplemented('init');
    }
}

module.exports = BlockmessConnector;