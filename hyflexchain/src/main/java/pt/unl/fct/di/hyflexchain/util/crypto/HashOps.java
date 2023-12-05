package pt.unl.fct.di.hyflexchain.util.crypto;

import java.security.MessageDigest;

public interface HashOps {
    MessageDigest update(MessageDigest md);

    static <T extends HashOps> MessageDigest updateArray(T[] array, MessageDigest md)
    {
        for (T t : array)
            t.update(md);

        return md;
    }
}
