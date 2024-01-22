package dk.stonemountain;

import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cmd.ExecuteJobsCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;
import org.camunda.bpm.quarkus.engine.extension.QuarkusProcessEngineConfiguration;

import io.quarkus.runtime.LaunchMode;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

@ApplicationScoped
public class CoreProcessEngineConfig extends QuarkusProcessEngineConfiguration { // NOSONAR

    public CoreProcessEngineConfig() {
        if (LaunchMode.DEVELOPMENT.equals(LaunchMode.current()) || LaunchMode.TEST.equals(LaunchMode.current())) {
            List<ProcessEnginePlugin> processEnginePlugins = new ArrayList<>();
            processEnginePlugins.add(new ClassLoaderProcessEnginePlugin());
            setProcessEnginePlugins(processEnginePlugins);
        }
    }
    static class ClassLoaderProcessEnginePlugin extends AbstractProcessEnginePlugin {
        @Override
        public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
            ClassLoader quarkusRuntimeClassloader = Thread.currentThread().getContextClassLoader();
            processEngineConfiguration.setCustomPreCommandInterceptorsTxRequired(Collections.singletonList(new CommandInterceptor() {
                @Override
                public <T> T execute(Command<T> command) {
                    if (command instanceof ExecuteJobsCmd) {
                        Thread.currentThread().setContextClassLoader(quarkusRuntimeClassloader);
                    }
                    return next.execute(command);
                }
            }));
        }
    }
}