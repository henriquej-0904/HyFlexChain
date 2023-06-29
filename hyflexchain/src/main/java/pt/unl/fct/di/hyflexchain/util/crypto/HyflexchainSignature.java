package pt.unl.fct.di.hyflexchain.util.crypto;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.InvalidAddressException;
import pt.unl.fct.di.hyflexchain.util.serializers.BytesSerializer;

/**
 * Represents a signature.
 */
public record HyflexchainSignature(
    Address address,
    SignatureAlgorithm signatureAlg,
    byte[] signature
) {

    public static final BytesSerializer<HyflexchainSignature> SERIALIZER =
        new HyflexchainSignature.Serializer();


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
    public static HyflexchainSignature sign(
        Address address,
        PrivateKey privKey,
        SignatureAlgorithm signatureAlg,
        ByteBuffer data
    ) throws InvalidKeyException, SignatureException
    {
        var sig = signatureAlg.signature();
        sig.initSign(privKey);
        sig.update(data);
        
        return new HyflexchainSignature(address, signatureAlg, sig.sign());
    }

    /**
     * Verify if this signature is valid.
     * @param data
     * @return true if this signature is valid.
     * @throws InvalidKeyException
     * @throws InvalidAddressException
     * @throws SignatureException
     */
    public boolean verify(ByteBuffer data) throws InvalidKeyException, InvalidAddressException, SignatureException
    {
        var sig = this.signatureAlg.signature();
        sig.initVerify(this.address.readPublicKey());
        sig.update(data);
        return sig.verify(this.signature);
    }

    public static class Serializer implements BytesSerializer<HyflexchainSignature>
    {
        private final BytesSerializer<byte[]> byteArraySerializer =
            BytesSerializer.primitiveArraySerializer(byte[].class);

        private final BytesSerializer<Address> addressSerializer =
            Address.SERIALIZER;

        @Override
        public Class<HyflexchainSignature> getType() {
            return HyflexchainSignature.class;
        }

        @Override
        public int serializedSize(HyflexchainSignature obj) {
            return addressSerializer.serializedSize(obj.address) + 1
            + byteArraySerializer.serializedSize(obj.signature);
        }

        @Override
        public ByteBuffer serialize(HyflexchainSignature obj, ByteBuffer buff) {
            buff = addressSerializer.serialize(obj.address, buff)
                .put(obj.signatureAlg.getAlgId());

            return byteArraySerializer.serialize(obj.signature, buff);
        }

        @Override
        public HyflexchainSignature deserialize(ByteBuffer buff) {
            try {
                return new HyflexchainSignature(
                    addressSerializer.deserialize(buff),
                    SignatureAlgorithm.decodeOrThrow(buff.get()),
                    byteArraySerializer.deserialize(buff)
                );
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
}
