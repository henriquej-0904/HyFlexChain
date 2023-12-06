package pt.unl.fct.di.hyflexchain.util.serializer.serializers.primitive;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

class CharSerializer implements ISerializer<Character> {

    static final ISerializer<Character> INSTANCE = new CharSerializer();

    @Override
    public void serialize(Character t, ByteBuf out) throws IOException {
        out.writeChar(t);
    }

    @Override
    public Character deserialize(ByteBuf in) throws IOException {
        return in.readChar();
    }
    
}
