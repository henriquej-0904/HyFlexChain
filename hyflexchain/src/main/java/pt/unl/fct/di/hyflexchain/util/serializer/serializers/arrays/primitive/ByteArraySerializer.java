package pt.unl.fct.di.hyflexchain.util.serializer.serializers.arrays.primitive;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

public class ByteArraySerializer implements ISerializer<byte[]> {

    public static final ByteArraySerializer INSTANCE =
        new ByteArraySerializer();

    public static final byte[] EMPTY_ARRAY = new byte[0];

    @Override
    public void serialize(byte[] t, ByteBuf out) throws IOException {
        out.writeInt(t.length);
        out.writeBytes(t);
    }

    @Override
    public byte[] deserialize(ByteBuf in) throws IOException {
        int size = in.readInt();

        if (size < 0)
            throw new IOException("Invalid size for array: " + size);

        if (size == 0)
            return EMPTY_ARRAY;

        byte[] array = new byte[size];
        in.readBytes(array);

        return array;
    }
    
}
