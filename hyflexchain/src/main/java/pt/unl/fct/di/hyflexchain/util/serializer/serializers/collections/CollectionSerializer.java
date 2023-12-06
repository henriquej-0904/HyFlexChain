package pt.unl.fct.di.hyflexchain.util.serializer.serializers.collections;

import java.io.IOException;
import java.util.Collection;
import java.util.function.IntFunction;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

public class CollectionSerializer<T extends Collection<E>, E>
implements ISerializer<T>
{
    protected final ISerializer<E> componentSerializer;

    protected final IntFunction<? extends T> createCollection;

    /**
     * @param componentSerializer
     */
    public CollectionSerializer(ISerializer<E> componentSerializer,
        IntFunction<? extends T> createCollection)
    {
        this.componentSerializer = componentSerializer;
        this.createCollection = createCollection;
    }

    @Override
    public void serialize(T t, ByteBuf out) throws IOException {
        int size = t.size();
        out.writeInt(size);

        for (E e : t)
            componentSerializer.serialize(e, out);
    }

    @Override
    public T deserialize(ByteBuf in) throws IOException {
        int size = in.readInt();

        if (size < 0)
            throw new IOException("Invalid size for collection: " + size);

        T coll = createCollection.apply(size);

        for (int i = 0; i < size; i++)
            coll.add(componentSerializer.deserialize(in));

        return coll;
    }
    
}
