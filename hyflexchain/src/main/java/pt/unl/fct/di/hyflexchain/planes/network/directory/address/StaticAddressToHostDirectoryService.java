package pt.unl.fct.di.hyflexchain.planes.network.directory.address;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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
            Map<String, Host> serializedMap =
                Utils.json.readValue(file, new TypeReference<Map<String, Host>>() {});

            Map<Address, Host> map = serializedMap.entrySet().stream()
                .collect(Collectors.toMap(
                    (e) -> Address.fromHexString(e.getKey()),
                    Entry::getValue,
                    (x, y) -> x,
                    () -> HashMap.<Address, Host>newHashMap(serializedMap.size())));

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

    /**
     * Create configuration directory service
     * @param args
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        File addressesFile = new File(args[0]);
        try (InputStream in = new FileInputStream(addressesFile)) {
            byte[] data = in.readAllBytes();
            Set<String> addresses =
                Utils.json.readValue(data, new TypeReference<LinkedHashMap<String, String>>() {})
                .keySet();
            
            var it = addresses.iterator();
            Map<String, Host> result = LinkedHashMap.newLinkedHashMap(addresses.size());
            for (int i = 0; i < addresses.size(); i++) {
                Host h = new Host("replica-" + i, 18000 + i);
                result.put(it.next(), h);
            }

            Utils.json.writeValue(System.out, result);
        }
    }


}
