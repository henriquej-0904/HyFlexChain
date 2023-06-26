package pt.unl.fct.di.hyflexchain.planes.network.directory;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;

/**
 * A Directory Service to map HyFlexChain addresses
 * to information needed by a specific consensus algorithm.
 */
public interface AddressDirectoryService<V> extends DirectoryService<Address, V>
{
    /**
     * The consensus mechanism associated with this direcotry service.
     * @return Consensus mechanism
     */
    ConsensusMechanism consensusMechanism();
}
