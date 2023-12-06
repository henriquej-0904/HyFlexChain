package pt.unl.fct.di.hyflexchain.util.serializer.serializers.primitive;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

class ByteSerializer implements ISerializer<Byte> {

    static final ISerializer<Byte> INSTANCE = new ByteSerializer();

    @Override
    public void serialize(Byte t, ByteBuf out) throws IOException {
        out.writeByte(t);
    }

    @Override
    public Byte deserialize(ByteBuf in) throws IOException {
        return in.readByte();
    }
    
}
