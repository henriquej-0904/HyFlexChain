package pt.unl.fct.di.hyflexchain.util.serializer.serializers.primitive;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

class FloatSerializer implements ISerializer<Float> {

    static final ISerializer<Float> INSTANCE = new FloatSerializer();

    @Override
    public void serialize(Float t, ByteBuf out) throws IOException {
        out.writeFloat(t);
    }

    @Override
    public Float deserialize(ByteBuf in) throws IOException {
        return in.readFloat();
    }
    
}
