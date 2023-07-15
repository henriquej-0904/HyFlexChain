package pt.unl.fct.di.hyflexchain.planes.network.directory.address;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.planes.network.Host;
import pt.unl.fct.di.hyflexchain.util.Utils;

/**
 * A static implementation of a directory service that maps
 * HyFlexChain address to the Host
 */
public class StaticAddressToHostDirectoryService implements AddressDirectoryService<Host>
{
    private final Map<Address, Host> map;

    protected StaticAddressToHostDirectoryService(int size)
    {
        this.map = HashMap.newHashMap(size);
    }

    protected StaticAddressToHostDirectoryService(Map<Address, Host> map)
    {
        this.map = map;
    }

    public static StaticAddressToHostDirectoryService fromJsonFile(File file)
    {
        try {
            Map<Address, Host> map =
                Utils.json.readValue(file, new TypeReference<Map<Address, Host>>() {});
            return new StaticAddressToHostDirectoryService(map);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Address Directory JSON file.",
                e);
        }
    }
    

    @Override
    public Optional<Host> lookup(Address key) {
        return Optional.ofNullable(this.map.get(key));
    }


}
