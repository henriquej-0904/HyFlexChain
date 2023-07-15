package pt.unl.fct.di.hyflexchain.util.reply;

import java.util.LinkedHashMap;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.util.crypto.HyflexchainSignature;

/**
 * Represents a reply signed by multiple participants.
 */
public class MultiSignedReply
{
    private final LinkedHashMap<Address, HyflexchainSignature> signatures;

    private final byte[] replyBytes;

    /**
     * Create a MultiSignedReply from a signed reply.
     * @param reply the reply
     * @param maxSignatures the max number of signatures
     */
    public MultiSignedReply(SignedReply reply, int maxSignatures) {
        this.signatures = LinkedHashMap.newLinkedHashMap(maxSignatures);
        this.replyBytes = reply.replyBytes;
        addSignature(reply.signature);
    }

    /**
     * @param signatures
     * @param replyBytes
     */
    public MultiSignedReply(LinkedHashMap<Address, HyflexchainSignature> signatures, byte[] replyBytes) {
        this.signatures = signatures;
        this.replyBytes = replyBytes;
    }

    /**
     * Add a signature.
     * @param signature
     * @return true if this is a new signature.
     */
    public boolean addSignature(HyflexchainSignature signature)
    {
        return this.signatures
            .putIfAbsent(signature.address(), signature) == null;
    }

    /**
     * @return the signatures
     */
    public LinkedHashMap<Address, HyflexchainSignature> getSignatures() {
        return signatures;
    }

    /**
     * Get the number of signatures in this reply.
     * @return The number of signatures.
     */
    public int getSignaturesCount()
    {
        return signatures.size();
    }


    /**
     * @return the replyBytes
     */
    public byte[] getReplyBytes() {
        return replyBytes;
    }
    
}
