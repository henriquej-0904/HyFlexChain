package pt.unl.fct.di.hyflexchain.planes.data.transaction;

import java.io.IOException;
import java.util.Objects;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import pt.unl.fct.di.hyflexchain.util.crypto.Crypto;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

/**
 * A wrapper for the {@link HyFlexChainTransaction} to provide efficient
 * access to the tx hash and serialized bytes.
 */
public record HashedTx(HyFlexChainTransaction tx, byte[] txHash) {

    public static final Serializer SERIALIZER = new Serializer();

    public static HashedTx deserialize(byte[] serializedTx) throws IOException
    {
        return new HashedTx(
            HyFlexChainTransaction.SERIALIZER.deserialize(Unpooled.wrappedBuffer(serializedTx)),
            Crypto.getSha256Digest().digest(serializedTx));
    }

    public static HashedTx from(HyFlexChainTransaction tx)
    {
        return new HashedTx(Objects.requireNonNull(tx), tx.update(Crypto.getSha256Digest()).digest());
    }

    public static HashedTx from(HyFlexChainTransaction tx, byte[] txHash)
    {
        return new HashedTx(Objects.requireNonNull(tx), Objects.requireNonNull(txHash));
    }

    /**
     * @return the tx
     */
    public HyFlexChainTransaction getTx() {
        return tx;
    }

    /**
     * @return the txHash
     */
    public byte[] getTxHash() {
        return txHash;
    }

    public static class Serializer implements ISerializer<HashedTx>
    {

        @Override
        public void serialize(HashedTx t, ByteBuf out) throws IOException {
            HyFlexChainTransaction.SERIALIZER.serialize(t.tx, out);
        }

        @Override
        public HashedTx deserialize(ByteBuf in) throws IOException {
            int beforeIndex = in.readerIndex();
            var tx = HyFlexChainTransaction.SERIALIZER.deserialize(in);
            int afterIndex = in.readerIndex();

            var md = Crypto.getSha256Digest();
            in.forEachByte(beforeIndex, afterIndex - beforeIndex, b -> {md.update(b);return true;});

            return new HashedTx(tx, md.digest());
        }
        
    }

}
