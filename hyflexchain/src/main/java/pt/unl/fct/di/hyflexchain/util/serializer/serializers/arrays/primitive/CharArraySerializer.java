package pt.unl.fct.di.hyflexchain.util.serializer.serializers.arrays.primitive;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

public class CharArraySerializer implements ISerializer<char[]> {

    public static final CharArraySerializer INSTANCE =
        new CharArraySerializer();

    public static final char[] EMPTY_ARRAY = new char[0];

    @Override
    public void serialize(char[] t, ByteBuf out) throws IOException {
        out.writeInt(t.length);

        for (char v : t) {
            out.writeChar(v);
        }
    }

    @Override
    public char[] deserialize(ByteBuf in) throws IOException {
        int size = in.readInt();

        if (size < 0)
            throw new IOException("Invalid size for array: " + size);

        if (size == 0)
            return EMPTY_ARRAY;

        char[] array = new char[size];

        for (int i = 0; i < size; i++)
            array[i] = in.readChar();

        return array;
    }
    
}
