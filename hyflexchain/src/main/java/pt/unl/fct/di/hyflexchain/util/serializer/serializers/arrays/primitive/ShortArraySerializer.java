package pt.unl.fct.di.hyflexchain.util.serializer.serializers.arrays.primitive;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

public class ShortArraySerializer implements ISerializer<short[]> {

    public static final ShortArraySerializer INSTANCE =
        new ShortArraySerializer();

    public static final short[] EMPTY_ARRAY = new short[0];

    @Override
    public void serialize(short[] t, ByteBuf out) throws IOException {
        out.writeInt(t.length);

        for (short v : t) {
            out.writeShort(v);
        }
    }

    @Override
    public short[] deserialize(ByteBuf in) throws IOException {
        int size = in.readInt();

        if (size < 0)
            throw new IOException("Invalid size for array: " + size);

        if (size == 0)
            return EMPTY_ARRAY;

        short[] array = new short[size];

        for (int i = 0; i < size; i++)
            array[i] = in.readShort();

        return array;
    }
    
}
