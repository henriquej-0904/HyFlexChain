package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.replylistener;

import java.io.IOException;
import java.nio.ByteBuffer;
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
import pt.unl.fct.di.hyflexchain.util.result.Result;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

public interface BftsmartConsensusResult extends Result<VerifiedTransactionsReply, byte[]>, BytesOps {

    public static final Serializer SERIALIZER =
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
    default SignedReply signReply(
            Address address,
            PrivateKey privKey,
            SignatureAlgorithm signatureAlg) throws InvalidKeyException, SignatureException
    {
        final byte[] data = new byte[serializedSize()];
        try {
            SERIALIZER.serialize(this, Unpooled.wrappedBuffer(data).setIndex(0, 0));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
        HyFlexChainSignature sig = HyFlexChainSignature.sign(address, privKey, signatureAlg, data);
        return new SignedReply(sig, data);
    }

    static BftsmartConsensusResult ok(VerifiedTransactionsReply result) {
        return new OkConsensusResult(result);
    }

    static BftsmartConsensusResult failed(byte[] result) {
        return new FailedConsensusResult(result);
    }

    class OkConsensusResult extends OkResult<VerifiedTransactionsReply, byte[]>
            implements BftsmartConsensusResult {

        public OkConsensusResult(VerifiedTransactionsReply result) {
            super(result);
        }

        @Override
        public int serializedSize() {
            return Byte.BYTES
                + getOkResult().serializedSize();
        }
    }

    class FailedConsensusResult extends FailedResult<VerifiedTransactionsReply, byte[]>
            implements BftsmartConsensusResult
    {
        public FailedConsensusResult(byte[] result) {
            super(result);
        }

        @Override
        public int serializedSize() {
            return Byte.BYTES
                + BytesOps.serializedSize(getFailedResult());
        }
    }

    public class Serializer implements ISerializer<BftsmartConsensusResult>
    {
        protected static final byte TRUE = 1;
        protected static final byte FALSE = 1;

        protected static final VerifiedTransactionsReply.Serializer okSerializer =
            VerifiedTransactionsReply.SERIALIZER;

        protected static final ISerializer<byte[]> failedSerializer =
            Utils.serializer.getArraySerializerByte();

        @Override
        public void serialize(BftsmartConsensusResult obj, ByteBuf buff) throws IOException {
            buff.writeBoolean(obj.isOk());

            if (obj.isOk())
                okSerializer.serialize(obj.getOkResult(), buff);
            else
                failedSerializer.serialize(obj.getFailedResult(), buff);
        }

        @Override
        public BftsmartConsensusResult deserialize(ByteBuf buff) throws IOException {
            try {
            boolean ok = buff.readBoolean();

            if (ok)
                return ok(VerifiedTransactionsReply.SERIALIZER.deserialize(buff));
            else
                return failed(failedSerializer.deserialize(buff));
        } catch (Exception e) {
            throw new IOException("Cannot deserialize a BftsmartConsensusResult", e);
        }
        }
        
    }
}
