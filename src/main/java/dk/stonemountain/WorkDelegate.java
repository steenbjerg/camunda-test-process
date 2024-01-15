package dk.stonemountain;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import dk.stonemountain.client.EchoResource;
import dk.stonemountain.client.EchoResult;
import jakarta.inject.Inject;

public class WorkDelegate {
    @Inject
    @RestClient
    EchoResource echoService;

    public void execute() {
        EchoResult result = echoService.echo();
    }
}