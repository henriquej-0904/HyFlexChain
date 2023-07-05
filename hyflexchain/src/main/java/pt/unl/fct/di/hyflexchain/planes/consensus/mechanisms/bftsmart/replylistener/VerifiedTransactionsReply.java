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
        byte[] prevBlockMerkleRootHash,
        byte[] merkleRootHash)
{
    public static final BytesSerializer<VerifiedTransactionsReply> SERIALIZER =
        new VerifiedTransactionsReply.Serializer();
    
    
    private static final BytesSerializer<byte[]> arraySerializer = BytesSerializer
            .primitiveArraySerializer(byte[].class);

    /**
     * Parse the signed reply.
     * 
     * @param reply The reply to parse
     * @return The parsed reply
     */
    public static VerifiedTransactionsReply fromReply(SignedReply reply) {
        ByteBuffer buff = ByteBuffer.wrap(reply.replyBytes);

        try {
            return new VerifiedTransactionsReply(
                    arraySerializer.deserialize(buff),
                    arraySerializer.deserialize(buff));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid VerifiedTransactionsReply", e);
        }
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
    public SignedReply signReply(
            Address address,
            PrivateKey privKey,
            SignatureAlgorithm signatureAlg) throws InvalidKeyException, SignatureException {
        ByteBuffer data = serializeReply();
        HyflexchainSignature sig = HyflexchainSignature.sign(address, privKey, signatureAlg, data);
        return new SignedReply(sig, data.array());
    }

    private ByteBuffer serializeReply() {
        int size = arraySerializer.serializedSize(prevBlockMerkleRootHash) +
                arraySerializer.serializedSize(merkleRootHash);

        ByteBuffer buff = ByteBuffer.allocate(size);
        buff = arraySerializer.serialize(prevBlockMerkleRootHash, buff);
        return arraySerializer.serialize(merkleRootHash, buff)
                .position(0);
    }

    static class Serializer implements BytesSerializer<VerifiedTransactionsReply> {

        @Override
        public Class<VerifiedTransactionsReply> getType() {
            return VerifiedTransactionsReply.class;
        }

        @Override
        public int serializedSize(VerifiedTransactionsReply obj) {
            return arraySerializer.serializedSize(obj.prevBlockMerkleRootHash) +
                    arraySerializer.serializedSize(obj.merkleRootHash);
        }

        @Override
        public ByteBuffer serialize(VerifiedTransactionsReply obj, ByteBuffer buff) {
            buff = arraySerializer.serialize(obj.prevBlockMerkleRootHash, buff);
            return arraySerializer.serialize(obj.merkleRootHash, buff);
        }

        @Override
        public VerifiedTransactionsReply deserialize(ByteBuffer buff) {
            try {
                return new VerifiedTransactionsReply(
                        arraySerializer.deserialize(buff),
                        arraySerializer.deserialize(buff));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid VerifiedTransactionsReply", e);
            }
        }

    }
}
