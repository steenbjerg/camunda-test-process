package dk.stonemountain;

import java.time.Duration;
import java.time.LocalDateTime;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.stonemountain.client.EchoResource;
import jakarta.inject.Inject;

public class WorkDelegate implements JavaDelegate {
    private static final Logger log = LoggerFactory.getLogger(WorkDelegate.class);

    @Inject
    @RestClient
    EchoResource echoService;
    
    @Override
	public void execute(DelegateExecution execution) throws Exception {
        LocalDateTime startTime = LocalDateTime.now();		
        log.debug("Task {} being executed}", this.getClass().getName());
		
		try {
            var result = echoService.echo();
            log.info("Result : {}", result);
		} finally {
			log.debug("Duration of {} : {}", this.getClass().getName(), Duration.between(startTime, LocalDateTime.now()));
		}
	}
}