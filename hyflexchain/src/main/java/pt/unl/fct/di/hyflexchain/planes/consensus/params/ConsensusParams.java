package pt.unl.fct.di.hyflexchain.planes.consensus.params;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;

/**
 * ConsensusParams
 */
public class ConsensusParams {

    private ConsensusMechanism mechanism;

    /**
     * 
     */
    public ConsensusParams() {
    }

    /**
     * @param mechanism
     */
    public ConsensusParams(ConsensusMechanism mechanism) {
        this.mechanism = mechanism;
    }

    
    /**
     * Parse the value as a ConsensusParams object.
     * @param value
     * @return ConsensusParams
     */
    public static ConsensusParams parse(String value)
    {
        /* try {
            return Utils.json.readValue(value, ConsensusParams.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        } */

        return new ConsensusParams(ConsensusMechanism.parse(value));
    }

    /**
     * @return the mechanism
     */
    public ConsensusMechanism getMechanism() {
        return mechanism;
    }

    /**
     * @param mechanism the mechanism to set
     */
    public void setMechanism(ConsensusMechanism mechanism) {
        this.mechanism = mechanism;
    }
}