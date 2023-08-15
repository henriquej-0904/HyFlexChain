package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.replylistener;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.SignatureException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.util.BytesOps;
import pt.unl.fct.di.hyflexchain.util.Utils;
import pt.unl.fct.di.hyflexchain.util.crypto.HyFlexChainSignature;
import pt.unl.fct.di.hyflexchain.util.crypto.SignatureAlgorithm;
import pt.unl.fct.di.hyflexchain.util.reply.SignedReply;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;


/**
 * A reply of one of the replicas in the committee.
 * This reply assures that a list of transactions was ordered
 * and is valid.
 */
public record VerifiedTransactionsReply(
        long nonce,
        byte[] prevBlockMerkleRootHash,
        byte[] merkleRootHash
) implements BytesOps
{
    public static final Serializer SERIALIZER =
        new VerifiedTransactionsReply.Serializer();

    /**
     * Parse the signed reply.
     * 
     * @param reply The reply to parse
     * @return The parsed reply
     */
    public static VerifiedTransactionsReply fromReply(SignedReply reply) throws IOException {
        final ByteBuf buff = Unpooled.wrappedBuffer(reply.replyBytes);
        return SERIALIZER.deserialize(buff);
    }

    /**
     * Create and a sign the reply.
     * 
     * @param address      The address of this node
     * @param privKey      The private key
     * @param signatureAlg The signature algorithm
     * @return The signed reply
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public SignedReply signReply(Address address, PrivateKey privKey,
        SignatureAlgorithm signatureAlg) throws InvalidKeyException, SignatureException
    {
        final byte[] data = new byte[serializedSize()];
        try {
            SERIALIZER.serialize(this, Unpooled.wrappedBuffer(data).setIndex(0, 0));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        HyFlexChainSignature sig = HyFlexChainSignature.sign(address, privKey, signatureAlg, data);
        return new SignedReply(sig, data);
    }

    @Override
    public int serializedSize() {
        return Long.BYTES
        + BytesOps.serializedSize(prevBlockMerkleRootHash)
        + BytesOps.serializedSize(merkleRootHash);
    }

    static class Serializer implements ISerializer<VerifiedTransactionsReply> {

        private static final ISerializer<byte[]> arraySerializer =
            Utils.serializer.getArraySerializerByte();
        
        @Override
        public void serialize(VerifiedTransactionsReply obj, ByteBuf buff) throws IOException {
            buff.writeLong(obj.nonce);
            arraySerializer.serialize(obj.prevBlockMerkleRootHash, buff);
            arraySerializer.serialize(obj.merkleRootHash, buff);
        }

        @Override
        public VerifiedTransactionsReply deserialize(ByteBuf buff) throws IOException {
            try {
                return new VerifiedTransactionsReply(
                        buff.readLong(),
                        arraySerializer.deserialize(buff),
                        arraySerializer.deserialize(buff));
            } catch (Exception e) {
                throw new IOException("Invalid VerifiedTransactionsReply", e);
            }
        }

    }
}
