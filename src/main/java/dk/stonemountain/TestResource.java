package dk.stonemountain;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/tests")
@Transactional
public class TestResource {
    public static record Process(String businessKey, String processDefinition, String version) {}

    private static final Logger log = LoggerFactory.getLogger(TestResource.class);
    private static final String PROCESS_DEFINITION = "test-process";	
    private static final String CURRENT_PROCESS_VERSION = "v1";

    private static AtomicLong counter = new AtomicLong(1);

	@Inject
	TaskService taskService;
	@Inject
	RepositoryService repositoryService;
	@Inject
	RuntimeService runtimeService;
	@Inject
	HistoryService historyService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Process startProcess() {
        log.info("Starting process");
        String id = startProcess(PROCESS_DEFINITION, CURRENT_PROCESS_VERSION, counter.getAndIncrement());
        return new Process(id, PROCESS_DEFINITION, CURRENT_PROCESS_VERSION);
    }

   	private String startProcess(String process, String version, Long businessKey) {
		log.debug("Starting process {}/{} with business key {} and variables", process, version, businessKey);
		var processDefinitionCandidates = repositoryService.createProcessDefinitionQuery()
			.processDefinitionKey(process)
			.list();

		processDefinitionCandidates.stream()
			.forEach(p -> log.debug("Process currently known: Id = {}, Key = {}, Name = {}, Version tag = {}, Version = {}", p.getDeploymentId(), p.getKey(), p.getName(), p.getVersionTag(), p.getVersion()));

		repositoryService.createProcessDefinitionQuery()
			.processDefinitionKey(process)
			.versionTag(version)
			.list()
			.forEach(p -> log.debug("Process of version known: Id = {}, Key = {}, Name = {}, Version tag = {}, Version = {}", p.getDeploymentId(), p.getKey(), p.getName(), p.getVersionTag(), p.getVersion()));
		
		var procDefToUse = repositoryService.createProcessDefinitionQuery()
			.processDefinitionKey(process)
			.versionTag(version)
			.list()
			.stream()
			.sorted((a, b) -> b.getVersion() - a.getVersion())
			.peek(p -> log.debug("Looking at Process Definition {}/{}/{}", p.getKey(), p.getVersionTag(), p.getVersion()))
			.findFirst()
			.orElseThrow(() -> new ProcessDefinitionNotFoundException(process, version));

            var variables = new HashMap<String, Object>();
		log.debug("Starting up process definition {} (key = {}, version = {}/{}) with variables: {}", procDefToUse.getId(), process, version, procDefToUse.getVersion(), variables);
		var processInstance = runtimeService.startProcessInstanceById(procDefToUse.getId(), businessKey.toString(), variables);
		log.debug("Process {} started", processInstance.getId());
		return processInstance.getId();
	}

    public static class ProcessDefinitionNotFoundException extends RuntimeException {
        private final String version;
        private final String process;

        public ProcessDefinitionNotFoundException(String process, String version) {
            super("Process " + process + " and version " + version + " not found");
            this.process = process;
            this.version = version;
        }

        public String getVersion() {
            return version;
        }

        public String getProcess() {
            return process;
        }
    }
}