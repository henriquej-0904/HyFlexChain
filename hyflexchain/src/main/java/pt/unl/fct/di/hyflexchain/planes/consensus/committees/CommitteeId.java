package pt.unl.fct.di.hyflexchain.planes.consensus.committees;

import org.apache.tuweni.bytes.Bytes;

import pt.unl.fct.di.hyflexchain.util.BytesOps;
import pt.unl.fct.di.hyflexchain.util.Utils;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

/**
 * Represents the identification of a committee.
 * 
 * @param id The id of a committee
 * @param blockHash The hash of a block that defines the committee
 */
public record CommitteeId(int id, Bytes blockHash) implements BytesOps {

    public static final ISerializer<CommitteeId> SERIALIZER =
        Utils.serializer.getRecordSerializer(
            CommitteeId.class,
            Utils.serializer.getSerializer(int.class),
            Utils.serializer.getSerializer(Bytes.class));
    
    public static final CommitteeId FIRST_COMMITTEE_ID =
        new CommitteeId(0, Bytes.EMPTY);

    @Override
    public int serializedSize() {
        return Integer.BYTES
            + BytesOps.serializedSize(blockHash);
    }

}
