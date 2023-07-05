package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.replylistener;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.SignatureException;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.util.crypto.HyflexchainSignature;
import pt.unl.fct.di.hyflexchain.util.crypto.SignatureAlgorithm;
import pt.unl.fct.di.hyflexchain.util.reply.SignedReply;
import pt.unl.fct.di.hyflexchain.util.result.Result;
import pt.unl.fct.di.hyflexchain.util.serializers.BytesSerializer;

public interface BftsmartConsensusResult extends Result<VerifiedTransactionsReply, byte[]> {

    public static final BytesSerializer<BftsmartConsensusResult> SERIALIZER =
        new BftsmartConsensusResult.Serializer();

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
    SignedReply signReply(
            Address address,
            PrivateKey privKey,
            SignatureAlgorithm signatureAlg) throws InvalidKeyException, SignatureException;

    static BftsmartConsensusResult fromReply(byte[] reply) {
        try {
            ByteBuffer buff = ByteBuffer.wrap(reply);
            boolean ok = buff.get() == 1;

            if (ok)
                return ok(VerifiedTransactionsReply.SERIALIZER.deserialize(buff));
            else
                return failed(
                        BytesSerializer.primitiveArraySerializer(byte[].class)
                                .deserialize(buff));
        } catch (Exception e) {
            throw new IllegalArgumentException("Reply is not a BftsmartConsensusResult", e);
        }

    }

    static BftsmartConsensusResult ok(VerifiedTransactionsReply result) {
        return new OkConsensusResult(result);
    }

    static BftsmartConsensusResult failed(byte[] result) {
        return new FailedConsensusResult(result);
    }

    class OkConsensusResult extends OkResult<VerifiedTransactionsReply, byte[]>
            implements BftsmartConsensusResult {
        private static final byte TRUE = 1;

        public OkConsensusResult(VerifiedTransactionsReply result) {
            super(result);
        }

        @Override
        public SignedReply signReply(Address address, PrivateKey privKey,
                SignatureAlgorithm signatureAlg)
                throws InvalidKeyException, SignatureException {
            var serializer = VerifiedTransactionsReply.SERIALIZER;
            ByteBuffer data = ByteBuffer.allocate(
                    1 + serializer.serializedSize(this.getOkResult()));

            data.put(TRUE);
            data = serializer.serialize(getOkResult(), data).position(0);

            HyflexchainSignature sig = HyflexchainSignature.sign(address, privKey, signatureAlg, data);
            return new SignedReply(sig, data.array());
        }
    }

    class FailedConsensusResult extends FailedResult<VerifiedTransactionsReply, byte[]>
            implements BftsmartConsensusResult {
        private static final byte FALSE = 1;

        public FailedConsensusResult(byte[] result) {
            super(result);
        }

        @Override
        public SignedReply signReply(Address address, PrivateKey privKey,
                SignatureAlgorithm signatureAlg)
                throws InvalidKeyException, SignatureException {
            var serializer = BytesSerializer.primitiveArraySerializer(byte[].class);
            ByteBuffer data = ByteBuffer.allocate(
                    1 + serializer.serializedSize(this.getFailedResult()));

            data.put(FALSE);
            data = serializer.serialize(getFailedResult(), data).position(0);

            HyflexchainSignature sig = HyflexchainSignature.sign(address, privKey, signatureAlg, data);
            return new SignedReply(sig, data.array());
        }
    }

    class Serializer implements BytesSerializer<BftsmartConsensusResult>
    {
        protected static final byte TRUE = 1;
        protected static final byte FALSE = 1;

        protected static final BytesSerializer<VerifiedTransactionsReply> okSerializer =
            VerifiedTransactionsReply.SERIALIZER;

        protected static final BytesSerializer<byte[]> failedSerializer =
            BytesSerializer.primitiveArraySerializer(byte[].class);


        @Override
        public Class<BftsmartConsensusResult> getType() {
            return BftsmartConsensusResult.class;
        }

        @Override
        public int serializedSize(BftsmartConsensusResult obj) {
            return 1 + (obj.isOk() ?
                okSerializer.serializedSize(obj.getOkResult()) :
                failedSerializer.serializedSize(obj.getFailedResult())
            );
        }

        @Override
        public ByteBuffer serialize(BftsmartConsensusResult obj, ByteBuffer buff) {
            buff.put(obj.isOk() ? TRUE : FALSE);
            return obj.isOk() ? okSerializer.serialize(obj.getOkResult(), buff) :
                failedSerializer.serialize(obj.getFailedResult(), buff);
        }

        @Override
        public BftsmartConsensusResult deserialize(ByteBuffer buff) {
            try {
            boolean ok = buff.get() == TRUE;

            if (ok)
                return ok(VerifiedTransactionsReply.SERIALIZER.deserialize(buff));
            else
                return failed(
                        BytesSerializer.primitiveArraySerializer(byte[].class)
                                .deserialize(buff));
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot deserialize a BftsmartConsensusResult", e);
        }
        }
        
    }
}
