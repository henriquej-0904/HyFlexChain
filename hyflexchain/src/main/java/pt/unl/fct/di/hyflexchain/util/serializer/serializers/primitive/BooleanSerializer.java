package pt.unl.fct.di.hyflexchain.util.serializer.serializers.primitive;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

class BooleanSerializer implements ISerializer<Boolean> {

    static final ISerializer<Boolean> INSTANCE = new BooleanSerializer();

    @Override
    public void serialize(Boolean t, ByteBuf out) throws IOException {
        out.writeBoolean(t);
    }

    @Override
    public Boolean deserialize(ByteBuf in) throws IOException {
        return in.readBoolean();
    }
    
}
