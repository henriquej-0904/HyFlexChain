package pt.unl.fct.di.hyflexchain.planes.network.directory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.util.Utils;

/**
 * A static implementation of the AddressDirectoryService.
 */
public class StaticAddressDirectoryService<V> implements AddressDirectoryService<V>
{
    private final ConsensusMechanism consensusMechanism;

    private final Map<Address, V> map;

    protected StaticAddressDirectoryService(ConsensusMechanism consensusMechanism, int size)
    {
        this.consensusMechanism = consensusMechanism;
        this.map = HashMap.newHashMap(size);
    }

    protected StaticAddressDirectoryService(ConsensusMechanism consensusMechanism,
        Map<Address, V> map)
    {
        this.consensusMechanism = consensusMechanism;
        this.map = map;
    }

    public static <V> StaticAddressDirectoryService<V> fromJsonFile(ConsensusMechanism consensusMechanism,
        File file)
    {
        try {
            Map<Address, V> map = Utils.json.readValue(file, new TypeReference<Map<Address, V>>() {});
            return new StaticAddressDirectoryService<>(consensusMechanism, map);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Address Directory JSON file.",
                e);
        }
    }
    

    @Override
    public Optional<V> lookup(Address key) {
        return Optional.ofNullable(this.map.get(key));
    }

    @Override
    public ConsensusMechanism consensusMechanism() {
        return this.consensusMechanism;
    }


}
