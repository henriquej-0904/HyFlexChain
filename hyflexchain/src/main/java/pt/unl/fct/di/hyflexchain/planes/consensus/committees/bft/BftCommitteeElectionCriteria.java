package pt.unl.fct.di.hyflexchain.planes.consensus.committees.bft;

import pt.unl.fct.di.hyflexchain.planes.consensus.committees.CommitteeElectionCriteria;

/**
 * The election criteria for a BFT Committee.
 */
public class BftCommitteeElectionCriteria extends CommitteeElectionCriteria
{

    public final int f;

    /**
     * @param size
     * @param sizeThreshold
     * @param randSource
     * @param validity
     * @param f
     */
    public BftCommitteeElectionCriteria(int size, int sizeThreshold,
        RandSource randSource, CommitteeValidity validity,
        int f) {
        super(size, sizeThreshold, randSource, validity);
        this.f = f;
    }


    /**
     * @param baseCriteria The base criteria for all committees
     * @param f            The maximum number of faulty replicas
     */
    public BftCommitteeElectionCriteria(CommitteeElectionCriteria baseCriteria, int f) {
        super(baseCriteria.size, baseCriteria.sizeThreshold,
            baseCriteria.randSource, baseCriteria.validity);
        
        this.f = f;
    }

}
