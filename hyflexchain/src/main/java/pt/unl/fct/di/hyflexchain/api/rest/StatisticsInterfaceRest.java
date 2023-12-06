package pt.unl.fct.di.hyflexchain.api.rest;

import java.util.EnumMap;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.util.stats.BlockStats;

@Path(StatisticsInterfaceRest.PATH)
public interface StatisticsInterfaceRest {
    static final String PATH="/hyflexchain/stats";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    EnumMap<ConsensusMechanism, BlockStats> blockStats();
}
