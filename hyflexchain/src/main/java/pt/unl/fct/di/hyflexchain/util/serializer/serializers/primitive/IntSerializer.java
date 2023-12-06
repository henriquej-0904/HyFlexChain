package pt.unl.fct.di.hyflexchain.util.serializer.serializers.primitive;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

class IntSerializer implements ISerializer<Integer> {

    static final ISerializer<Integer> INSTANCE = new IntSerializer();

    @Override
    public void serialize(Integer t, ByteBuf out) throws IOException {
        out.writeInt(t);
    }

    @Override
    public Integer deserialize(ByteBuf in) throws IOException {
        return in.readInt();
    }
    
}
