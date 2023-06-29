package pt.unl.fct.di.hyflexchain.util.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Optional;
import java.util.stream.Stream;

import pt.unl.fct.di.hyflexchain.util.Utils;

/**
 * The currently supported signature algorithms.
 */
public enum SignatureAlgorithm
{
    /**
     * The eliptic curve digital signature algorithm
     */
    SHA256withECDSA("SHA256withECDSA", (byte) 1);

    private final String name;

    private final byte algId;


    /**
     * @param name
     * @param algId
     */
    private SignatureAlgorithm(String name, byte algId) {
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

    /**
     * Create a signature instance.
     * @return A signature instance of this type.
     */
    public Signature signature()
    {
        try {
            return Crypto.createSignatureInstance(name);
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e.getMessage(), e);
        }
    }

    public static Optional<SignatureAlgorithm> decode(String name)
    {
        return Stream.of(values())
            .filter((alg) -> alg.name.equalsIgnoreCase(name))
            .findAny();
    }

    public static SignatureAlgorithm decodeOrThrow(String name) throws NoSuchAlgorithmException
    {
        return decode(name)
            .orElseThrow(() -> noSuchAlgorithmException(name));
    }

    public static Optional<SignatureAlgorithm> decode(byte algId)
    {
        return Stream.of(values())
            .filter((alg) -> alg.algId == algId)
            .findAny();
    }

    public static SignatureAlgorithm decodeOrThrow(byte algId) throws NoSuchAlgorithmException
    {
        return decode(algId)
            .orElseThrow(() -> noSuchAlgorithmException(algId));
    }

    public static SignatureAlgorithm fromPublicKey(PublicKey key)
    {
        return decode(key.getAlgorithm())
            .orElseThrow(() -> Utils.toError(noSuchAlgorithmException(key.getAlgorithm())));
    }

    public static NoSuchAlgorithmException noSuchAlgorithmException(String name)
    {
        return new NoSuchAlgorithmException("Unsupported signature algorithm name: " + name);
    }

    public static NoSuchAlgorithmException noSuchAlgorithmException(byte algId)
    {
        return new NoSuchAlgorithmException("Invalid signature algorithm identifier: " + algId);
    }
}
