'use strict';

import BlockmessConnector from './BlockmessConnector';

async function ConnectorFactory(workerIndex) {
    const connector = new BlockmessConnector(workerIndex);

    // initialize the connector for the worker processes
    if (workerIndex >= 0) {
        await connector.init(true);
    }

    return connector;
}

const _ConnectorFactory = ConnectorFactory;
export { _ConnectorFactory as ConnectorFactory };