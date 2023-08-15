package pt.unl.fct.di.hyflexchain.util.crypto;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Optional;
import java.util.stream.Stream;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.Utils;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

/**
 * The currently supported crypto algorithms.
 */
public enum CryptoAlgorithm
{
    /**
     * The eliptic curve algorithm
     */
    EC("EC", (byte) 1);

    public static final ISerializer<CryptoAlgorithm> SERIALIZER =
        new Serializer();

    private final String name;

    private final byte algId;


    /**
     * @param name
     * @param algId
     */
    private CryptoAlgorithm(String name, byte algId) {
        this.name = name;
        this.algId = algId;
    }

    /**
     * The name of this algorithm.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * The id of this algorithm.
     * @return the algId
     */
    public byte getAlgId() {
        return algId;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static Optional<CryptoAlgorithm> decode(String name)
    {
        return Stream.of(values())
            .filter((alg) -> alg.name.equalsIgnoreCase(name))
            .findAny();
    }

    public static CryptoAlgorithm decodeOrThrow(String name) throws NoSuchAlgorithmException
    {
        return decode(name)
            .orElseThrow(() -> noSuchAlgorithmException(name));
    }

    public static Optional<CryptoAlgorithm> decode(byte algId)
    {
        return Stream.of(values())
            .filter((alg) -> alg.algId == algId)
            .findAny();
    }

    public static CryptoAlgorithm decodeOrThrow(byte algId) throws NoSuchAlgorithmException
    {
        return decode(algId)
            .orElseThrow(() -> noSuchAlgorithmException(algId));
    }

    public static CryptoAlgorithm fromPublicKey(PublicKey key)
    {
        return decode(key.getAlgorithm())
            .orElseThrow(() -> Utils.toError(noSuchAlgorithmException(key.getAlgorithm())));
    }

    public static NoSuchAlgorithmException noSuchAlgorithmException(String name)
    {
        return new NoSuchAlgorithmException("Unsupported crypto algorithm name: " + name);
    }

    public static NoSuchAlgorithmException noSuchAlgorithmException(byte algId)
    {
        return new NoSuchAlgorithmException("Invalid crypto algorithm identifier: " + algId);
    }

    static final class Serializer implements ISerializer<CryptoAlgorithm>
    {
        @Override
        public void serialize(CryptoAlgorithm t, ByteBuf out) throws IOException {
            out.writeByte(t.algId);
        }

        @Override
        public CryptoAlgorithm deserialize(ByteBuf in) throws IOException {
            try {
                return decodeOrThrow(in.readByte());
            } catch (NoSuchAlgorithmException e) {
                throw new IOException(e.getMessage(), e);
            }
        }
    }
}
