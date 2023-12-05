package pt.unl.fct.di.hyflexchain.util.serializer.serializers.arrays.primitive;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

public class FloatArraySerializer implements ISerializer<float[]> {

    public static final FloatArraySerializer INSTANCE =
        new FloatArraySerializer();

    public static final float[] EMPTY_ARRAY = new float[0];

    @Override
    public void serialize(float[] t, ByteBuf out) throws IOException {
        out.writeInt(t.length);

        for (float v : t) {
            out.writeFloat(v);
        }
    }

    @Override
    public float[] deserialize(ByteBuf in) throws IOException {
        int size = in.readInt();

        if (size < 0)
            throw new IOException("Invalid size for array: " + size);

        if (size == 0)
            return EMPTY_ARRAY;

        float[] array = new float[size];

        for (int i = 0; i < size; i++)
            array[i] = in.readFloat();

        return array;
    }
    
}
