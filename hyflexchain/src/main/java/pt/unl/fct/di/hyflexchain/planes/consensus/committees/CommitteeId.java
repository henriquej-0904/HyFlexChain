package pt.unl.fct.di.hyflexchain.planes.consensus.committees;

/**
 * Represents the identification of a committee.
 * 
 * @param id The id of a committee
 * @param blockHash The hash of a block that defines the committee
 */
public record CommitteeId(int id, String blockHash) {
    
    public static final CommitteeId FIRST_COMMITTEE_ID =
        new CommitteeId(0, "null");

}
