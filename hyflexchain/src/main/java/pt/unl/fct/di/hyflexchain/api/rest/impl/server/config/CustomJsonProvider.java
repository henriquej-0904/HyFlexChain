package pt.unl.fct.di.hyflexchain.api.rest.impl.server.config;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;

import pt.unl.fct.di.hyflexchain.util.Utils;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class CustomJsonProvider extends JacksonJsonProvider {
    public CustomJsonProvider() {
        super();
        setMapper(Utils.json);
    }
}
