package dk.stonemountain.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("echo")
@RegisterRestClient(configKey = "echo-service")
public interface EchoResource {
    @Path("get/json")
    @Produces(MediaType.APPLICATION_JSON)
    EchoResult echo();
}
