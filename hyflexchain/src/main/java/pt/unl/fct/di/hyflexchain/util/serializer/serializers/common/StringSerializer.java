package pt.unl.fct.di.hyflexchain.util.serializer.serializers.common;

import java.io.IOException;
import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;
import pt.unl.fct.di.hyflexchain.util.serializer.serializers.arrays.primitive.ByteArraySerializer;

public class StringSerializer implements ISerializer<String> {

    public static final ISerializer<String> INSTANCE =
        new StringSerializer();

    private static final Charset CHARSET = Charset.defaultCharset();

    private static final ISerializer<byte[]> BYTE_ARRAY_SERIALIZER
        = ByteArraySerializer.INSTANCE;

    @Override
    public void serialize(String t, ByteBuf out) throws IOException {
        BYTE_ARRAY_SERIALIZER.serialize(t.getBytes(CHARSET), out);
    }

    @Override
    public String deserialize(ByteBuf in) throws IOException {
        return new String(BYTE_ARRAY_SERIALIZER.deserialize(in), CHARSET);
    }
    
}
