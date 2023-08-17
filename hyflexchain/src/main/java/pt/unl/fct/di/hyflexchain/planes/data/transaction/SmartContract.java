package pt.unl.fct.di.hyflexchain.planes.data.transaction;

import java.security.MessageDigest;
import java.security.Signature;
import java.security.SignatureException;

import pt.unl.fct.di.hyflexchain.planes.execution.SmartContractAddress;
import pt.unl.fct.di.hyflexchain.util.BytesOps;
import pt.unl.fct.di.hyflexchain.util.Utils;
import pt.unl.fct.di.hyflexchain.util.crypto.HashOps;
import pt.unl.fct.di.hyflexchain.util.crypto.SignatureOps;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

/**
 * A smart contract in a transaction used to create, revoke or
 * execute. If the transaction is of type TRANSFER then
 * it can either provide the address (id) or the code of the contract,
 * never both.
 * 
 * @param id   The address of the smart contract (mandatory for creation and
 *             revoke)
 * @param code The code of the smart contract (mandatory for create)
 */
public record SmartContract(Address id, byte[] code) implements BytesOps, HashOps, SignatureOps {
    public static final ISerializer<SmartContract> SERIALIZER =
        Utils.serializer.getRecordSerializer(
            SmartContract.class,
            Address.SERIALIZER,
            Utils.serializer.getArraySerializerByte());

    private static final byte[] EMPTY_CODE = new byte[0];

    public static SmartContract create(byte[] code) {
        return new SmartContract(SmartContractAddress.random(), code);
    }

    public static SmartContract reference(Address id) {
        return new SmartContract(id, EMPTY_CODE);
    }

    public static SmartContract code(byte[] code) {
        return new SmartContract(Address.NULL_ADDRESS, code);
    }

    public boolean isAddressProvided() {
        return !id.isNullAddress();
    }

    public boolean isCodeProvided() {
        return code.length > 0;
    }

    @Override
    public Signature update(Signature sig) throws SignatureException {
        id.update(sig);
        sig.update(code);
        return sig;
    }

    @Override
    public MessageDigest update(MessageDigest md) {
        id.update(md);
        Utils.toBytes(code.length);
        md.update(code);
        return md;
    }

    @Override
    public int serializedSize() {
        return id.serializedSize() + BytesOps.serializedSize(code);
    }
}
