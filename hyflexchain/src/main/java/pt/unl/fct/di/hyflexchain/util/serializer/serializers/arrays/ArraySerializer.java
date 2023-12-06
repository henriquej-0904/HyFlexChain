package pt.unl.fct.di.hyflexchain.util.serializer.serializers.arrays;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.function.IntFunction;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

public class ArraySerializer<T> implements ISerializer<T[]> {

    private final IntFunction<? extends T[]> newArray;

    private final T[] emptyArray;

    private final ISerializer<T> componentSerializer;


    /**
     * @param componentType
     * @param componentSerializer
     */
    @SuppressWarnings("unchecked")
    public ArraySerializer(Class<? extends T> componentType, ISerializer<T> componentSerializer) {
        this.newArray = (i) -> (T[]) Array.newInstance(componentType, i);
        this.emptyArray = this.newArray.apply(0);
        this.componentSerializer = componentSerializer;
    }

    @Override
    public void serialize(T[] t, ByteBuf out) throws IOException {
        out.writeInt(t.length);

        for (T v : t) {
            this.componentSerializer.serialize(v, out);
        }
    }

    @Override
    public T[] deserialize(ByteBuf in) throws IOException {
        int size = in.readInt();

        if (size < 0)
            throw new IOException("Invalid size for array: " + size);

        if (size == 0)
            return this.emptyArray;

        T[] array = this.newArray.apply(size);

        for (int i = 0; i < size; i++)
            array[i] = this.componentSerializer.deserialize(in);

        return array;
    }
    
}
