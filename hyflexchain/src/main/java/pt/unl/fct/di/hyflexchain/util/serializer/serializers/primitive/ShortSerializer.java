package pt.unl.fct.di.hyflexchain.util.serializer.serializers.primitive;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

class ShortSerializer implements ISerializer<Short> {

    static final ISerializer<Short> INSTANCE = new ShortSerializer();

    @Override
    public void serialize(Short t, ByteBuf out) throws IOException {
        out.writeShort(t);
    }

    @Override
    public Short deserialize(ByteBuf in) throws IOException {
        return in.readShort();
    }
    
}
