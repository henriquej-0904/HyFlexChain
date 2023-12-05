package pt.unl.fct.di.hyflexchain.util.serializer.serializers.arrays.primitive;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

public class BooleanArraySerializer implements ISerializer<boolean[]> {

    public static final BooleanArraySerializer INSTANCE =
        new BooleanArraySerializer();

    public static final boolean[] EMPTY_ARRAY = new boolean[0];

    @Override
    public void serialize(boolean[] t, ByteBuf out) throws IOException {
        out.writeInt(t.length);

        for (boolean b : t) {
            out.writeBoolean(b);
        }
    }

    @Override
    public boolean[] deserialize(ByteBuf in) throws IOException {
        int size = in.readInt();

        if (size < 0)
            throw new IOException("Invalid size for array: " + size);

        if (size == 0)
            return EMPTY_ARRAY;

        boolean[] array = new boolean[size];

        for (int i = 0; i < size; i++)
            array[i] = in.readBoolean();

        return array;
    }
    
}
