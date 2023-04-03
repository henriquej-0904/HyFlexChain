'use strict';

const BlockmessConnector = require('./BlockmessConnector');

async function connectorFactory(workerIndex) {
    const connector = new BlockmessConnector(workerIndex);

    // initialize the connector for the worker processes
    if (workerIndex >= 0) {
        await connector.init(true);
    }

    return connector;
}

module.exports.ConnectorFactory = connectorFactory;