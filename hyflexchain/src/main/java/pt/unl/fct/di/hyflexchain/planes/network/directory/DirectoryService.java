package pt.unl.fct.di.hyflexchain.planes.network.directory;

import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Directory Service to map keys
 * to information.
 */
public interface DirectoryService<K, V>
{
    /**
     * Lookup a key and get the associated
     * information, if it exists.
     * @param key The key to lookup.
     * @return An optional of the requested information.
     */
    Optional<V> lookup(K key);

    /**
     * Lookup a group of keys and get the associated
     * information, if it exists.
     * If there are failed lookups, the result
     * map will not contain an entry for those keys.
     * @param keys The keys to lookup.
     * @return A map with the result of the lookup
     */
    default Map<K, V> lookup(K[] keys)
    {
        Map<K, V> res = Stream.of(keys)
            .map((k) -> Map.entry(k, lookup(k)))
            .filter((entry) -> entry.getValue().isPresent())
            .map((entry) -> Map.entry(entry.getKey(), entry.getValue().get()))
            .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue));

        return res;
    }
}
