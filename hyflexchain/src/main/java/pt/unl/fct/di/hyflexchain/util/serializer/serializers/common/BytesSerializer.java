package pt.unl.fct.di.hyflexchain.util.serializer.serializers.common;

import java.io.IOException;

import org.apache.tuweni.bytes.Bytes;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;
import pt.unl.fct.di.hyflexchain.util.serializer.serializers.arrays.primitive.ByteArraySerializer;

public class BytesSerializer implements ISerializer<Bytes> {

    public static final BytesSerializer INSTANCE = new BytesSerializer();

    protected static final ISerializer<byte[]> byteArraySerializer =
        ByteArraySerializer.INSTANCE;

    @Override
    public void serialize(Bytes t, ByteBuf out) throws IOException {
        byteArraySerializer.serialize(t.toArrayUnsafe(), out);
    }

    @Override
    public Bytes deserialize(ByteBuf in) throws IOException {
        return Bytes.wrap(byteArraySerializer.deserialize(in));
    }
    
}
