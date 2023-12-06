package pt.unl.fct.di.hyflexchain.util.serializer.serializers.arrays.primitive;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

public class LongArraySerializer implements ISerializer<long[]> {

    public static final LongArraySerializer INSTANCE =
        new LongArraySerializer();

    public static final long[] EMPTY_ARRAY = new long[0];

    @Override
    public void serialize(long[] t, ByteBuf out) throws IOException {
        out.writeInt(t.length);

        for (long v : t) {
            out.writeLong(v);
        }
    }

    @Override
    public long[] deserialize(ByteBuf in) throws IOException {
        int size = in.readInt();

        if (size < 0)
            throw new IOException("Invalid size for array: " + size);

        if (size == 0)
            return EMPTY_ARRAY;

        long[] array = new long[size];

        for (int i = 0; i < size; i++)
            array[i] = in.readLong();

        return array;
    }
    
}
