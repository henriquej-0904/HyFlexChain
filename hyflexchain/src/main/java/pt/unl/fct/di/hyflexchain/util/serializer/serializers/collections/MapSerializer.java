package pt.unl.fct.di.hyflexchain.util.serializer.serializers.collections;

import java.io.IOException;
import java.util.Map;
import java.util.function.IntFunction;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

public class MapSerializer<K, V, T extends Map<K, V>> implements ISerializer<T>
{
    protected final ISerializer<K> kSerializer;

    protected final ISerializer<V> vSerializer;

    protected final IntFunction<T> createMap;

    /**
     * @param kSerializer
     * @param vSerializer
     * @param createMap
     */
    public MapSerializer(ISerializer<K> kSerializer, ISerializer<V> vSerializer,
        IntFunction<T> createMap)
    {
        this.kSerializer = kSerializer;
        this.vSerializer = vSerializer;
        this.createMap = createMap;
    }

    @Override
    public void serialize(T t, ByteBuf out) throws IOException {
        out.writeInt(t.size());

        for (var entry : t.entrySet()) {
            kSerializer.serialize(entry.getKey(), out);
            vSerializer.serialize(entry.getValue(), out);
        }
    }

    @Override
    public T deserialize(ByteBuf in) throws IOException {
        int size = in.readInt();

        if (size < 0)
            throw new IOException("Invalid size for collection: " + size);

        T map = createMap.apply(size);

        for (int i = 0; i < size; i++)
            map.put(kSerializer.deserialize(in), vSerializer.deserialize(in));

        return map;
    }
    
}
