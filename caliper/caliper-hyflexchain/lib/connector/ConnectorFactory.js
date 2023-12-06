'use strict';

const HyFlexChainConnector = require('./HyFlexChainConnector');

async function connectorFactory(workerIndex) {
    const connector = new HyFlexChainConnector(workerIndex);

    // initialize the connector for the worker processes
    if (workerIndex >= 0) {
        await connector.init(true);
    }

    return connector;
}

module.exports.ConnectorFactory = connectorFactory;