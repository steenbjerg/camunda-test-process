package dk.stonemountain;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/tests")
public class TestResource {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Process startProcess() {
        return "Hello from RESTEasy Reactive";
    }
}
