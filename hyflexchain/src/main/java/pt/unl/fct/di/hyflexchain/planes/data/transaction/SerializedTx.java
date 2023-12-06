package pt.unl.fct.di.hyflexchain.planes.data.transaction;

import java.io.IOException;

import org.apache.tuweni.bytes.Bytes;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.BytesOps;
import pt.unl.fct.di.hyflexchain.util.Utils;
import pt.unl.fct.di.hyflexchain.util.crypto.Crypto;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

/**
 * Represents a serialized transaction with its hash
 */
public record SerializedTx(Bytes hash, byte[] serialized) implements BytesOps {

    public static final Serializer SERIALIZER = new Serializer();

    public static SerializedTx from(HyFlexChainTransaction tx) throws IOException
    {
        return from(BytesOps.serialize(tx, HyFlexChainTransaction.SERIALIZER));
    }

    public static SerializedTx from(byte[] serialized)
    {
        return new SerializedTx(
            Bytes.wrap(Crypto.getSha256Digest().digest(serialized)), serialized);
    }


    @Override
    public int serializedSize() {
        return BytesOps.serializedSize(serialized);
    }

    public HyFlexChainTransaction deserialize() throws IOException
    {
        return HyFlexChainTransaction.SERIALIZER.deserialize(serialized);
    }

    public static class Serializer implements ISerializer<SerializedTx>
    {
        protected static final ISerializer<byte[]> byteArraySerializer =
            Utils.serializer.getArraySerializerByte();

        @Override
        public void serialize(SerializedTx t, ByteBuf out) throws IOException {
            byteArraySerializer.serialize(t.serialized, out);
        }

        @Override
        public SerializedTx deserialize(ByteBuf in) throws IOException {
            byte[] tx = byteArraySerializer.deserialize(in);
            return new SerializedTx(Bytes.wrap(Crypto.getSha256Digest().digest(tx)), tx);
        }
        
    }
    
}
