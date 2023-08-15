package pt.unl.fct.di.hyflexchain.util.serializer.serializers.arrays.primitive;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

public class DoubleArraySerializer implements ISerializer<double[]> {

    public static final DoubleArraySerializer INSTANCE =
        new DoubleArraySerializer();

    public static final double[] EMPTY_ARRAY = new double[0];

    @Override
    public void serialize(double[] t, ByteBuf out) throws IOException {
        out.writeInt(t.length);

        for (double v : t) {
            out.writeDouble(v);
        }
    }

    @Override
    public double[] deserialize(ByteBuf in) throws IOException {
        int size = in.readInt();

        if (size < 0)
            throw new IOException("Invalid size for array: " + size);

        if (size == 0)
            return EMPTY_ARRAY;

        double[] array = new double[size];

        for (int i = 0; i < size; i++)
            array[i] = in.readDouble();

        return array;
    }
    
}
