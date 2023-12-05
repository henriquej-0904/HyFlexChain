package pt.unl.fct.di.hyflexchain.api.rest.impl.server.config;

import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;

public class MarshallingFeature implements Feature {

    @Override
    public boolean configure(FeatureContext context) {
        context.register(CustomJsonProvider.class,
            MessageBodyReader.class, MessageBodyWriter.class);
        return true;
    }
    
}
