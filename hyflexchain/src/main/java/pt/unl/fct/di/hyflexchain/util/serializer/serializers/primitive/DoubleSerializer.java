package pt.unl.fct.di.hyflexchain.util.serializer.serializers.primitive;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

class DoubleSerializer implements ISerializer<Double> {

    static final ISerializer<Double> INSTANCE = new DoubleSerializer();

    @Override
    public void serialize(Double t, ByteBuf out) throws IOException {
        out.writeDouble(t);
    }

    @Override
    public Double deserialize(ByteBuf in) throws IOException {
        return in.readDouble();
    }
    
}
