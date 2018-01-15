Pipeline Spawner
================

Automatic build system for KRR-compliant applications.

The goals of this project are:

 * Spawn build jobs on a Kubernetes cluster whenever a project is updated, using git web-hooks;
 
 * Monitor running jobs with a web interface, using lift.
 
How To
------

Quickly run using Gradle's Jetty plugin:

  `./gradlew jettyRun`

Build an executable JAR with all dependencies:

  `./gradlew dist`
  
Which you can run with:
  
  `java -jar build/libs/pipeline-spawner-all.jar`
  
After building, test results will be available in 

  `build/reports/tests/index.html`
  
Run the Selenium acceptance test with

  `./gradlew acceptanceTest`
  
