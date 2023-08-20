package pt.unl.fct.di.hyflexchain.api.rest.settings;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path(PrivateSettingsInterface.PATH)
public interface PrivateSettingsInterface {
    static final String PATH="/hyflexchain/private/settings";

    /**
     * Reset the state of the HyFlexChain System, i.e.
     * it deletes the ledger and all components that
     * depend on it are reset (smart contracts, etc).
     * This method is useful for testing purposes.
     */
    @Path("/reset")
    @GET
    void reset();
}
