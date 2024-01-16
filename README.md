# camunda-test-process

Sample project for demonstrating a problem with Camunda in Quarkus. The problem occurs when the process definition contains a asynchronious service tasks. Actually, it looks like the problem occurs whenever Camunda has to create a thread.
The problem requires a the Java Delegate to invoke a Microprofile Rest-Client. When the service tasks is invoked in a asynchronious delegate, then the Java Delegate can not load the rest-client Java class.