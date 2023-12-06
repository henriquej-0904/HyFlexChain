package pt.unl.fct.di.hyflexchain.util.reply;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.SignatureException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.InvalidAddressException;
import pt.unl.fct.di.hyflexchain.util.BytesOps;
import pt.unl.fct.di.hyflexchain.util.Utils;
import pt.unl.fct.di.hyflexchain.util.crypto.HyFlexChainSignature;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

/**
 * Represents a reply which is signed by a specific crypto
 * algorithm.
 */
public class SignedReply implements BytesOps
{
    public static final Serializer SERIALIZER =
        new SignedReply.Serializer();

    public final HyFlexChainSignature signature;

    public final byte[] replyBytes;

    /**
     * Create a signed reply
     * @param signature
     * @param replyBytes
     */
    public SignedReply(HyFlexChainSignature signature, byte[] replyBytes) {
        this.signature = signature;
        this.replyBytes = replyBytes;
    }

    /**
     * Verify the signature
     * @return true if the signature is valid.
     * @throws InvalidKeyException
     * @throws SignatureException
     * @throws InvalidAddressException
     */
    public boolean verifySignature() throws InvalidKeyException, SignatureException, InvalidAddressException
    {
        return this.signature.verify(replyBytes);
    }

    /**
     * Serialize this object.
     * @return The serialized object.
     */
    public byte[] serialize()
    {
        try {
            var array = new byte[serializedSize()];
            SERIALIZER.serialize(this, Unpooled.wrappedBuffer(array).setIndex(0, 0));
            return array;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public int serializedSize() {
        return signature.serializedSize() +
            BytesOps.serializedSize(replyBytes);
    }

    public static class Serializer implements ISerializer<SignedReply>
    {
        private final ISerializer<byte[]> byteArraySerializer =
            Utils.serializer.getArraySerializerByte();

        @Override
        public void serialize(SignedReply t, ByteBuf out) throws IOException {
            HyFlexChainSignature.SERIALIZER.serialize(t.signature, out);
            byteArraySerializer.serialize(t.replyBytes, out);
        }

        @Override
        public SignedReply deserialize(ByteBuf in) throws IOException {
            return new SignedReply(
                HyFlexChainSignature.SERIALIZER.deserialize(in),
                byteArraySerializer.deserialize(in)
            );
        }
    }
}
