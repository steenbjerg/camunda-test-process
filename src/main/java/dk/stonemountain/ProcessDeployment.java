package dk.stonemountain;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.quarkus.engine.extension.event.CamundaEngineStartupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProcessDeployment {
    protected static final Logger log = LoggerFactory.getLogger(ProcessDeployment.class);

    @Inject
    RepositoryService repositoryService;

    public void deploy(@Observes CamundaEngineStartupEvent startupEvent) {
        log.info("begin deployment: {}", startupEvent);
        try {
            removeDeployments();
            listDeployments();
            listProcesses();
            deployBpmnProcesses();
            listDeployments();
            listProcesses();
        } catch (Exception e) {
            log.error("Could not deploy process bpmn to camunda", e);
            throw new RuntimeException("Could not deploy process bpmn to camunda", e);
        } finally {
            log.info("deployment completed");
        }
    }

    private void deployBpmnProcesses() throws IOException {
        deployBpmnProcess("/", "test-process-v1.bpmn", "test-process");
    }

    private void deployBpmnProcess(String path, String processFile, String processDefinitionKey) throws IOException {
        String filePath = path + processFile;
        String processBpmn = null;
        try (InputStream processIs = ProcessDeployment.class.getResourceAsStream(filePath)) {
            processBpmn = new String(processIs.readAllBytes(), StandardCharsets.UTF_8);
            log.info("Bpmn process loaded with hashcode {} and size {}", HexFormat.of().toHexDigits(processBpmn.hashCode()), processBpmn.length());
        }

        try (InputStream processBpmnIs = new ByteArrayInputStream(processBpmn.getBytes(StandardCharsets.UTF_8))) {
            DeploymentWithDefinitions deployment = repositoryService.createDeployment()
                .enableDuplicateFiltering(true)
                .name(processDefinitionKey)
                .addInputStream(processFile, processBpmnIs)
                .source(processFile)
                .deployWithResult();
            log.info("Deployment created with id {}, name {} and source {}. No of process definitions: {}, Deployment date: {}", deployment.getId(), deployment.getName(), deployment.getSource(), deployment.getDeployedProcessDefinitions() != null ? deployment.getDeployedProcessDefinitions().size() : "None", deployment.getDeploymentTime());
            if (deployment.getDeployedProcessDefinitions() != null) {
                deployment.getDeployedProcessDefinitions()
                    .stream()
                    .forEach(d -> log.info("Process Def in deployment: Id: {}, Deployment Id: {}, Key: {}, Name: {}, Resource: {}", d.getId(), d.getDeploymentId(), d.getKey(), d.getName(), d.getResourceName()));
            }
        }
    }

    public void listProcesses() {
        List<ProcessDefinition> processes = repositoryService.createProcessDefinitionQuery()
            .orderByDeploymentTime()
            .asc()
            .list();
        log.info("No of processes: {}", processes.size());
        processes.stream().forEach(p -> log.info("Process '{}/{}/{}'' in deployment {}", p.getName(), p.getKey(), p.getVersionTag(), p.getDeploymentId())); 
    }

    public void listDeployments() {
        var list = repositoryService.createDeploymentQuery()
            .orderByDeploymentTime()
            .asc()
            .list();
        log.info("Number of existing deployments: {}", list.size());
        list.stream().forEach(d -> log.info("Existing deployment id = {} with name {} and source {} at time {}", d.getId(), d.getName(), d.getSource(), d.getDeploymentTime()));
    }

    public void removeDeployments() {
        var list = repositoryService.createDeploymentQuery()
            .orderByDeploymentTime()
            .asc()
            .list();
        log.info("Number of existing deployments: {}", list.size());
        list.stream().forEach(d -> log.info("Existing deployment id = {} with name {} and source {} at time {}", d.getId(), d.getName(), d.getSource(), d.getDeploymentTime()));
 
        list.forEach(d -> {
            if (d.getName() == null || d.getName().startsWith("test")) {
                repositoryService.deleteDeployment(d.getId(), true);
                log.info("Removed deployment {}", d.getName());
            }
        });
    }

}