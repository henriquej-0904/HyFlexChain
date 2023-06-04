package pt.unl.fct.di.hyflexchain.planes.consensus.params;

import java.util.Optional;

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
     * @return an optional of ConsensusParams.
     */
    public static Optional<ConsensusParams> parse(String value)
    {
        try {
            var res = Utils.json.readValue(value, ConsensusParams.class);
            return Optional.of(res);
        } catch (JsonProcessingException e) {
            return Optional.empty();
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