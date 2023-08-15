package pt.unl.fct.di.hyflexchain.util.crypto;

import java.security.Signature;
import java.security.SignatureException;

public interface SignatureOps {
    Signature update(Signature sig) throws SignatureException;

    static <T extends SignatureOps> Signature updateArray(T[] array, Signature sig) throws SignatureException
    {
        for (T t : array)
            t.update(sig);

        return sig;
    }
}
