package pt.unl.fct.di.hyflexchain.planes.consensus.params;

import com.fasterxml.jackson.core.JsonProcessingException;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.util.Utils;

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
        try {
            return Utils.json.readValue(value, ConsensusParams.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
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