package pt.unl.fct.di.hyflexchain.util.crypto;

import java.io.IOException;

import org.apache.tuweni.bytes.Bytes;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

/**
 * A record of an object and the hash of it.
 */
public record HashedObject<T>(Bytes hash, T obj) {

    public static <T> ISerializer<HashedObject<T>> createSerializer(ISerializer<T> componentSerializer)
    {
        return new Serializer<>(componentSerializer);
    }

    public static class Serializer<T> implements ISerializer<HashedObject<T>>
    {

        protected final ISerializer<T> componentSerializer;

        /**
         * @param componentSerializer
         */
        public Serializer(ISerializer<T> componentSerializer) {
            this.componentSerializer = componentSerializer;
        }

        @Override
        public void serialize(HashedObject<T> t, ByteBuf out) throws IOException {
            componentSerializer.serialize(t.obj, out);
        }

        @Override
        public HashedObject<T> deserialize(ByteBuf in) throws IOException {
            int beforeIndex = in.readerIndex();
            T obj = componentSerializer.deserialize(in);
            int afterIndex = in.readerIndex();

            var md = Crypto.getSha256Digest();
            in.forEachByte(beforeIndex, afterIndex - beforeIndex, b -> {md.update(b);return true;});

            return new HashedObject<>(Bytes.wrap(md.digest()), obj);
        }
    }
}
