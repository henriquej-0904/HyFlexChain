package pt.unl.fct.di.hyflexchain.planes.network.directory.address;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.planes.network.directory.DirectoryService;

/**
 * A Directory Service to map HyFlexChain addresses
 * to information needed by the HyFlexChain system.
 */
public interface AddressDirectoryService<V> extends DirectoryService<Address, V>
{

}
