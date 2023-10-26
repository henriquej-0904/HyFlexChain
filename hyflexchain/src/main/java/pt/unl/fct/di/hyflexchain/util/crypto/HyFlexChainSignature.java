package pt.unl.fct.di.hyflexchain.util.crypto;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.SignatureException;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.InvalidAddressException;
import pt.unl.fct.di.hyflexchain.util.BytesOps;
import pt.unl.fct.di.hyflexchain.util.Utils;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

/**
 * Represents a signature.
 * 
 * @param address The address of the node that signed
 * @param signatureAlg The signature algorithm used to sign
 * @param signature The signature result
 */
public record HyFlexChainSignature(
    Address address,
    SignatureAlgorithm signatureAlg,
    byte[] signature
) implements BytesOps {

    public static final Serializer SERIALIZER =
        new HyFlexChainSignature.Serializer();


    /**
     * Create a signature
     * @param address
     * @param privKey
     * @param signatureAlg
     * @param data
     * @return The created signature
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public static HyFlexChainSignature sign(
        Address address,
        PrivateKey privKey,
        SignatureAlgorithm signatureAlg,
        byte[] data
    ) throws InvalidKeyException, SignatureException
    {
        var sig = signatureAlg.signature();
        sig.initSign(privKey);
        sig.update(data);
        
        return new HyFlexChainSignature(address, signatureAlg, sig.sign());
    }

    /**
     * Verify if this signature is valid.
     * @param data
     * @return true if this signature is valid.
     * @throws InvalidKeyException
     * @throws InvalidAddressException
     * @throws SignatureException
     */
    public boolean verify(byte[] data) throws InvalidKeyException, InvalidAddressException, SignatureException
    {
        var sig = this.signatureAlg.signature();
        sig.initVerify(this.address.readPublicKey());
        sig.update(data);
        return sig.verify(this.signature);
    }


    @Override
    public int serializedSize() {
        return address.serializedSize()
            + signatureAlg.serializedSize()
            + BytesOps.serializedSize(signature);
    }

    public static class Serializer implements ISerializer<HyFlexChainSignature>
    {
        private final ISerializer<byte[]> byteArraySerializer =
            Utils.serializer.getArraySerializerByte();

        @Override
        public void serialize(HyFlexChainSignature t, ByteBuf out) throws IOException {
            Address.SERIALIZER.serialize(t.address, out);
            SignatureAlgorithm.SERIALIZER.serialize(t.signatureAlg, out);
            byteArraySerializer.serialize(t.signature, out);
        }

        @Override
        public HyFlexChainSignature deserialize(ByteBuf in) throws IOException {
            return new HyFlexChainSignature(
                Address.SERIALIZER.deserialize(in),
                SignatureAlgorithm.SERIALIZER.deserialize(in),
                byteArraySerializer.deserialize(in)
            );
        }
    }
}
