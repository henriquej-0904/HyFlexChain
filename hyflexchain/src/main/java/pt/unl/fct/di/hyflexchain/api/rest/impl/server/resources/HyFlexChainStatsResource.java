package pt.unl.fct.di.hyflexchain.api.rest.impl.server.resources;

import java.util.EnumMap;

import jakarta.inject.Singleton;
import pt.unl.fct.di.hyflexchain.api.rest.StatisticsInterfaceRest;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.util.stats.BlockStats;

@Singleton
public class HyFlexChainStatsResource implements StatisticsInterfaceRest {

    @Override
    public EnumMap<ConsensusMechanism, BlockStats> blockStats() {
        return BlockStats.stats;
    }
    
}
