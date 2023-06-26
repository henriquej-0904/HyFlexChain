package pt.unl.fct.di.hyflexchain.planes.network.directory;

import java.util.Optional;

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
}
