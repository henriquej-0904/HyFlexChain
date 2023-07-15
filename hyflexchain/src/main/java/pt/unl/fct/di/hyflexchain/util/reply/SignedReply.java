package pt.unl.fct.di.hyflexchain.util.reply;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.SignatureException;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.InvalidAddressException;
import pt.unl.fct.di.hyflexchain.util.crypto.HyflexchainSignature;
import pt.unl.fct.di.hyflexchain.util.serializers.BytesSerializer;

/**
 * Represents a reply which is signed by a specific crypto
 * algorithm.
 */
public class SignedReply
{
    public static final BytesSerializer<SignedReply> SERIALIZER =
        new SignedReply.Serializer();

    public final HyflexchainSignature signature;

    public final byte[] replyBytes;

    /**
     * Create a signed reply
     * @param signature
     * @param replyBytes
     */
    public SignedReply(HyflexchainSignature signature, byte[] replyBytes) {
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
        return this.signature.verify(ByteBuffer.wrap(replyBytes));
    }

    /**
     * Serialize this object.
     * @return The serialized object.
     */
    public byte[] serialize()
    {
        return SERIALIZER.serialize(
            this,
            ByteBuffer.allocate(SERIALIZER.serializedSize(this))
        ).array();
    }

    public static class Serializer implements BytesSerializer<SignedReply>
    {
        private final BytesSerializer<byte[]> byteArraySerializer =
            BytesSerializer.primitiveArraySerializer(byte[].class);

        @Override
        public Class<SignedReply> getType() {
            return SignedReply.class;
        }

        @Override
        public int serializedSize(SignedReply obj) {
            return HyflexchainSignature.SERIALIZER.serializedSize(obj.signature)
                + byteArraySerializer.serializedSize(obj.replyBytes);
        }

        @Override
        public ByteBuffer serialize(SignedReply obj, ByteBuffer buff) {
            buff = HyflexchainSignature.SERIALIZER.serialize(obj.signature, buff);
            return byteArraySerializer.serialize(obj.replyBytes, buff);
        }

        @Override
        public SignedReply deserialize(ByteBuffer buff) {
            try {
                return new SignedReply(
                    HyflexchainSignature.SERIALIZER.deserialize(buff),
                    byteArraySerializer.deserialize(buff)
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize: SignedReply", e);
            }
        }
    }
}
