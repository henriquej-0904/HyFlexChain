package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.replylistener;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.SignatureException;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.util.crypto.HyflexchainSignature;
import pt.unl.fct.di.hyflexchain.util.crypto.SignatureAlgorithm;
import pt.unl.fct.di.hyflexchain.util.reply.SignedReply;
import pt.unl.fct.di.hyflexchain.util.serializers.BytesSerializer;

/**
 * A reply of one of the replicas in the committee.
 * This reply assures that a list of transactions was ordered
 * and is valid.
 */
public record VerifiedTransactionsReply(
        long nonce,
        byte[] prevBlockMerkleRootHash,
        byte[] merkleRootHash)
{
    public static final BytesSerializer<VerifiedTransactionsReply> SERIALIZER =
        new VerifiedTransactionsReply.Serializer();

    /**
     * Parse the signed reply.
     * 
     * @param reply The reply to parse
     * @return The parsed reply
     */
    public static VerifiedTransactionsReply fromReply(SignedReply reply) {
        final ByteBuffer buff = ByteBuffer.wrap(reply.replyBytes);
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
        final ByteBuffer data = SERIALIZER.serialize(this);
        HyflexchainSignature sig = HyflexchainSignature.sign(address, privKey, signatureAlg, data);
        return new SignedReply(sig, data.array());
    }

    static class Serializer implements BytesSerializer<VerifiedTransactionsReply> {

        private static final BytesSerializer<byte[]> arraySerializer = BytesSerializer
            .primitiveArraySerializer(byte[].class);

        @Override
        public Class<VerifiedTransactionsReply> getType() {
            return VerifiedTransactionsReply.class;
        }

        @Override
        public int serializedSize(VerifiedTransactionsReply obj) {
            return Long.BYTES + arraySerializer.serializedSize(obj.prevBlockMerkleRootHash) +
                    arraySerializer.serializedSize(obj.merkleRootHash);
        }

        @Override
        public ByteBuffer serialize(VerifiedTransactionsReply obj, ByteBuffer buff) {
            buff.putLong(obj.nonce);
            buff = arraySerializer.serialize(obj.prevBlockMerkleRootHash, buff);
            return arraySerializer.serialize(obj.merkleRootHash, buff);
        }

        @Override
        public VerifiedTransactionsReply deserialize(ByteBuffer buff) {
            try {
                return new VerifiedTransactionsReply(
                        buff.getLong(),
                        arraySerializer.deserialize(buff),
                        arraySerializer.deserialize(buff));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid VerifiedTransactionsReply", e);
            }
        }

    }
}
