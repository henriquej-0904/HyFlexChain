package pt.unl.fct.di.hyflexchain.util.serializer.serializers.primitive;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

class LongSerializer implements ISerializer<Long> {

    static final ISerializer<Long> INSTANCE = new LongSerializer();

    @Override
    public void serialize(Long t, ByteBuf out) throws IOException {
        out.writeLong(t);
    }

    @Override
    public Long deserialize(ByteBuf in) throws IOException {
        return in.readLong();
    }
    
}
