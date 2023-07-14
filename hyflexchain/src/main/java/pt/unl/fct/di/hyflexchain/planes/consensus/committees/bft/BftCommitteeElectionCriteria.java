package pt.unl.fct.di.hyflexchain.planes.consensus.committees.bft;

import pt.unl.fct.di.hyflexchain.planes.consensus.committees.CommitteeElectionCriteria;

/**
 * The election criteria for a BFT Committee.
 */
public class BftCommitteeElectionCriteria extends CommitteeElectionCriteria
{

    protected int f;

    public BftCommitteeElectionCriteria()
    {

    }

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
        super(baseCriteria.getSize(), baseCriteria.getSizeThreshold(),
            baseCriteria.getRandSource(), baseCriteria.getValidity());
        
        this.f = f;
    }

    /**
     * @return the f
     */
    public int getF() {
        return f;
    }

    /**
     * @param f the f to set
     */
    public void setF(int f) {
        this.f = f;
    }

    /**
     * Get the quorum size for this committee = 2 * {@link #getF()} + 1
     * @return The size for the qurom of this committee
     */
    public int consensusQuorum()
    {
        return 2*f+1;
    }
}
