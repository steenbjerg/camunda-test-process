# camunda-test-process

Show case project for demonstrating a problem with Camunda in Quarkus when running QuarkusDev (./gradlew quarkusDev). The problem occurs when the process definition contains a asynchronious service tasks. Actually, it looks like the problem occurs whenever Camunda has to create a thread.
The problem requires a Java Delegate to invoke a Microprofile Rest-Client. When the service tasks is invoked in a asynchronious delegate, then the Java Delegate can not load the rest-client Java class.

## Camunda Integration

* The current version of Camunda is 7.20.0. I have also tested Camunda 7.20.2-ee.
* The current version of Quarkus is 3.2.9.Final (It does not work with the latest version of Quarkus 3.6.4)

### Camunda Related Information

* Defines a single BPMN proces in file src/resources/test-process-v1. It's name and id is _test-process_ and the version is _v1_.
* The class ProcessDeployment handles deployment of the above process.
* One sample Java delegate called WorkDelegate. It invokes an microprofile rest-client call EchoResource (see interface).
* application.properties contains Quarkus and Camunda configuration (it uses a Postgresql database for Camunda).
* Class TestResource starts the Camunda process instance. 

## Reproduce problem

* Start: ./gradlew quarkusDev
* Invoke URL http://localhost:8080/tests (see class TestResource). It will fail.
* Deselect the asynchronous continuations (before) at the service task named _work_ inside the test-process-v1.bpmn file. Restart ./gradlew quarkusDev. Now the process works as intented.

The log output when the failure occurs is:

```text
17:18:56 WARN  [or.ca.bp.en.jobexecutor] (pool-8-thread-4) ENGINE-14006 Exception while executing job f2b0e691-b48a-11ee-8973-f48c50edbe21: : org.camunda.bpm.engine.ProcessEngineException: Error while evaluating expression: ${work}. Cause: Error injecting dk.stonemountain.client.EchoResource dk.stonemountain.WorkDelegate.echoService
        at org.camunda.bpm.engine.impl.el.JuelExpression.getValue(JuelExpression.java:76)
        at org.camunda.bpm.engine.impl.el.JuelExpression.getValue(JuelExpression.java:51)
        at org.camunda.bpm.engine.impl.bpmn.behavior.ServiceTaskDelegateExpressionActivityBehavior$3.call(ServiceTaskDelegateExpressionActivityBehavior.java:107)
        at org.camunda.bpm.engine.impl.bpmn.behavior.ServiceTaskDelegateExpressionActivityBehavior$3.call(ServiceTaskDelegateExpressionActivityBehavior.java:102)
        at org.camunda.bpm.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior.executeWithErrorPropagation(AbstractBpmnActivityBehavior.java:90)
        at org.camunda.bpm.engine.impl.bpmn.behavior.ServiceTaskDelegateExpressionActivityBehavior.performExecution(ServiceTaskDelegateExpressionActivityBehavior.java:127)
        at org.camunda.bpm.engine.impl.bpmn.behavior.TaskActivityBehavior.execute(TaskActivityBehavior.java:69)
        at org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationActivityExecute$2.callback(PvmAtomicOperationActivityExecute.java:61)
        at org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationActivityExecute$2.callback(PvmAtomicOperationActivityExecute.java:50)
        at org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl.continueIfExecutionDoesNotAffectNextOperation(PvmExecutionImpl.java:2082)
        at org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationActivityExecute.execute(PvmAtomicOperationActivityExecute.java:42)
        at org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationActivityExecute.execute(PvmAtomicOperationActivityExecute.java:31)
        at org.camunda.bpm.engine.impl.interceptor.AtomicOperationInvocation.execute(AtomicOperationInvocation.java:99)
        at org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext.invokeNext(CommandInvocationContext.java:130)
        at org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext.performNext(CommandInvocationContext.java:110)
        at org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext.performOperation(CommandInvocationContext.java:85)
        at org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity.performOperation(ExecutionEntity.java:622)
        at org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity.performOperation(ExecutionEntity.java:596)
        at org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl$6.callback(PvmExecutionImpl.java:2021)
        at org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl$6.callback(PvmExecutionImpl.java:2018)
        at org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl.continueExecutionIfNotCanceled(PvmExecutionImpl.java:2088)
        at org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl.dispatchDelayedEventsAndPerformOperation(PvmExecutionImpl.java:2037)
        at org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl.dispatchDelayedEventsAndPerformOperation(PvmExecutionImpl.java:2018)
        at org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationTransitionNotifyListenerStart.eventNotificationsCompleted(PvmAtomicOperationTransitionNotifyListenerStart.java:61)
        at org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationTransitionNotifyListenerStart.eventNotificationsCompleted(PvmAtomicOperationTransitionNotifyListenerStart.java:30)
        at org.camunda.bpm.engine.impl.core.operation.AbstractEventAtomicOperation.execute(AbstractEventAtomicOperation.java:70)
        at org.camunda.bpm.engine.impl.interceptor.AtomicOperationInvocation.execute(AtomicOperationInvocation.java:99)
        at org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext.invokeNext(CommandInvocationContext.java:130)
        at org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext.performNext(CommandInvocationContext.java:110)
        at org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext.performOperation(CommandInvocationContext.java:85)
        at org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext.performOperation(CommandInvocationContext.java:75)
        at org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity.performOperationSync(ExecutionEntity.java:631)
        at org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity.performOperationSync(ExecutionEntity.java:606)
        at org.camunda.bpm.engine.impl.core.operation.AbstractEventAtomicOperation.execute(AbstractEventAtomicOperation.java:66)
        at org.camunda.bpm.engine.impl.interceptor.AtomicOperationInvocation.execute(AtomicOperationInvocation.java:99)
        at org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext.invokeNext(CommandInvocationContext.java:130)
        at org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext.performNext(CommandInvocationContext.java:110)
        at org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext.performOperation(CommandInvocationContext.java:85)
        at org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext.performOperation(CommandInvocationContext.java:75)
        at org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity.performOperationSync(ExecutionEntity.java:631)
        at org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity.performOperationSync(ExecutionEntity.java:606)
        at org.camunda.bpm.engine.impl.core.operation.AbstractEventAtomicOperation.execute(AbstractEventAtomicOperation.java:66)
        at org.camunda.bpm.engine.impl.interceptor.AtomicOperationInvocation.execute(AtomicOperationInvocation.java:99)
        at org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext.invokeNext(CommandInvocationContext.java:130)
        at org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext.performNext(CommandInvocationContext.java:110)
        at org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext.performOperation(CommandInvocationContext.java:85)
        at org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext.performOperation(CommandInvocationContext.java:75)
        at org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity.performOperationSync(ExecutionEntity.java:631)
        at org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity.performOperationSync(ExecutionEntity.java:606)
        at org.camunda.bpm.engine.impl.core.operation.AbstractEventAtomicOperation.execute(AbstractEventAtomicOperation.java:66)
        at org.camunda.bpm.engine.impl.interceptor.AtomicOperationInvocation.execute(AtomicOperationInvocation.java:99)
        at org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext.invokeNext(CommandInvocationContext.java:130)
        at org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext.performNext(CommandInvocationContext.java:110)
        at org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext.performOperation(CommandInvocationContext.java:85)
        at org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity.performOperation(ExecutionEntity.java:622)
        at org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity.performOperation(ExecutionEntity.java:596)
        at org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationTransitionCreateScope.scopeCreated(PvmAtomicOperationTransitionCreateScope.java:38)
        at org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationCreateScope.execute(PvmAtomicOperationCreateScope.java:53)
        at org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationCreateScope.execute(PvmAtomicOperationCreateScope.java:27)
        at org.camunda.bpm.engine.impl.interceptor.AtomicOperationInvocation.execute(AtomicOperationInvocation.java:99)
        at org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext.invokeNext(CommandInvocationContext.java:130)
        at org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext.performNext(CommandInvocationContext.java:117)
        at org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext.performOperation(CommandInvocationContext.java:85)
        at org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext.performOperation(CommandInvocationContext.java:75)
        at org.camunda.bpm.engine.impl.jobexecutor.AsyncContinuationJobHandler.execute(AsyncContinuationJobHandler.java:81)
        at org.camunda.bpm.engine.impl.jobexecutor.AsyncContinuationJobHandler.execute(AsyncContinuationJobHandler.java:40)
        at org.camunda.bpm.engine.impl.persistence.entity.JobEntity.execute(JobEntity.java:133)
        at org.camunda.bpm.engine.impl.cmd.ExecuteJobsCmd.execute(ExecuteJobsCmd.java:110)
        at org.camunda.bpm.engine.impl.cmd.ExecuteJobsCmd.execute(ExecuteJobsCmd.java:43)
        at org.camunda.bpm.engine.impl.interceptor.CommandExecutorImpl.execute(CommandExecutorImpl.java:28)
        at org.camunda.bpm.engine.impl.interceptor.CommandContextInterceptor.execute(CommandContextInterceptor.java:110)
        at org.camunda.bpm.engine.impl.interceptor.AbstractTransactionInterceptor.execute(AbstractTransactionInterceptor.java:54)
        at org.camunda.bpm.engine.impl.interceptor.ProcessApplicationContextInterceptor.execute(ProcessApplicationContextInterceptor.java:70)
        at org.camunda.bpm.engine.impl.interceptor.CommandCounterInterceptor.execute(CommandCounterInterceptor.java:35)
        at org.camunda.bpm.engine.impl.interceptor.LogInterceptor.execute(LogInterceptor.java:33)
        at org.camunda.bpm.engine.impl.interceptor.ExceptionCodeInterceptor.execute(ExceptionCodeInterceptor.java:55)
        at org.camunda.bpm.engine.impl.jobexecutor.ExecuteJobHelper.executeJob(ExecuteJobHelper.java:57)
        at org.camunda.bpm.engine.impl.jobexecutor.ExecuteJobsRunnable.executeJob(ExecuteJobsRunnable.java:110)
        at org.camunda.bpm.engine.impl.jobexecutor.ExecuteJobsRunnable.run(ExecuteJobsRunnable.java:71)
        at io.smallrye.context.impl.wrappers.SlowContextualRunnable.run(SlowContextualRunnable.java:19)
        at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
        at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
        at java.base/java.lang.Thread.run(Thread.java:1583)
Caused by: java.lang.RuntimeException: Error injecting dk.stonemountain.client.EchoResource dk.stonemountain.WorkDelegate.echoService
        at dk.stonemountain.WorkDelegate_Bean.doCreate(Unknown Source)
        at dk.stonemountain.WorkDelegate_Bean.create(Unknown Source)
        at dk.stonemountain.WorkDelegate_Bean.get(Unknown Source)
        at dk.stonemountain.WorkDelegate_Bean.get(Unknown Source)
        at io.quarkus.arc.impl.ArcContainerImpl.beanInstanceHandle(ArcContainerImpl.java:561)
        at io.quarkus.arc.impl.BeanManagerImpl.getReference(BeanManagerImpl.java:67)
        at org.camunda.bpm.engine.cdi.impl.util.ProgrammaticBeanLookup.getContextualReference(ProgrammaticBeanLookup.java:118)
        at org.camunda.bpm.engine.cdi.impl.util.ProgrammaticBeanLookup.lookup(ProgrammaticBeanLookup.java:65)
        at org.camunda.bpm.engine.cdi.impl.util.ProgrammaticBeanLookup.lookup(ProgrammaticBeanLookup.java:54)
        at org.camunda.bpm.engine.cdi.impl.el.CdiResolver.getValue(CdiResolver.java:65)
        at org.camunda.bpm.impl.juel.jakarta.el.CompositeELResolver.getValue(CompositeELResolver.java:136)
        at org.camunda.bpm.impl.juel.AstIdentifier.eval(AstIdentifier.java:81)
        at org.camunda.bpm.impl.juel.AstEval.eval(AstEval.java:50)
        at org.camunda.bpm.impl.juel.AstNode.getValue(AstNode.java:26)
        at org.camunda.bpm.impl.juel.TreeValueExpression.getValue(TreeValueExpression.java:112)
        at org.camunda.bpm.engine.impl.delegate.ExpressionGetInvocation.invoke(ExpressionGetInvocation.java:40)
        at org.camunda.bpm.engine.impl.delegate.DelegateInvocation.proceed(DelegateInvocation.java:58)
        at org.camunda.bpm.engine.impl.delegate.DefaultDelegateInterceptor.handleInvocationInContext(DefaultDelegateInterceptor.java:92)
        at org.camunda.bpm.engine.impl.delegate.DefaultDelegateInterceptor.handleInvocation(DefaultDelegateInterceptor.java:63)
        at org.camunda.bpm.engine.impl.el.JuelExpression.getValue(JuelExpression.java:60)
        ... 82 more
Caused by: jakarta.enterprise.inject.CreationException: java.lang.ClassNotFoundException: dk.stonemountain.client.EchoResource
        at dk.stonemountain.client.EchoResource$$CDIWrapper_Bean.create(Unknown Source)
        at dk.stonemountain.client.EchoResource$$CDIWrapper_Bean.create(Unknown Source)
        at io.quarkus.arc.impl.AbstractSharedContext.createInstanceHandle(AbstractSharedContext.java:113)
        at io.quarkus.arc.impl.AbstractSharedContext$1.get(AbstractSharedContext.java:37)
        at io.quarkus.arc.impl.AbstractSharedContext$1.get(AbstractSharedContext.java:34)
        at io.quarkus.arc.impl.LazyValue.get(LazyValue.java:26)
        at io.quarkus.arc.impl.ComputingCache.computeIfAbsent(ComputingCache.java:69)
        at io.quarkus.arc.impl.AbstractSharedContext.get(AbstractSharedContext.java:34)
        at dk.stonemountain.client.EchoResource$$CDIWrapper_Bean.get(Unknown Source)
        at dk.stonemountain.client.EchoResource$$CDIWrapper_Bean.get(Unknown Source)
        ... 102 more
Caused by: java.lang.ClassNotFoundException: dk.stonemountain.client.EchoResource
        at io.quarkus.bootstrap.classloading.QuarkusClassLoader.loadClass(QuarkusClassLoader.java:489)
        at io.quarkus.bootstrap.classloading.QuarkusClassLoader.loadClass(QuarkusClassLoader.java:466)
        at java.base/java.lang.Class.forName0(Native Method)
        at java.base/java.lang.Class.forName(Class.java:534)
        at java.base/java.lang.Class.forName(Class.java:513)
        at dk.stonemountain.client.EchoResource$$CDIWrapper.<init>(Unknown Source)
        at dk.stonemountain.client.EchoResource$$CDIWrapper_Bean.doCreate(Unknown Source)
        ... 112 more
```

## References

* https://docs.camunda.org/manual/develop/user-guide/quarkus-integration - how to integrate Camunda into Quarkus
* https://forum.camunda.io/t/classnotfoundexception-for-mp-restclient-interfaces-since-quarkus-version-2-8-2/38327 - another developer having the same problem and a workaround.