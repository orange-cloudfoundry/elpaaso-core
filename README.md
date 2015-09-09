# elpaaso-core

[![Build Status](https://travis-ci.org/Orange-OpenSource/elpaaso-core.svg?branch=master)](https://travis-ci.org/Orange-OpenSource/elpaaso-core)

El PaaSo core: runtime and ui

More context and introduction into the [elpaaso repo](https://github.com/Orange-OpenSource/elpaaso)

# Building and running tests

Builds using maven 3.3 or higher, checkout travis-ci config for reference.
* Unit test run out of the box without additional config
* Integration tests (ITs) are launched through the ``runITs`` maven target
   * ITs may require additional dependencies to execute (CF, maven repo, optionally orange private dbaas service)
   * The full list of supported credentials for tests is at [credentials-development_specific.properties](https://github.com/Orange-OpenSource/elpaaso-core/blob/faa5578e136cd81a31ad7a2b711f5db655b3fe9f/cloud-commons-parent/cloud-commons-test-configurations/src/main/resources/com/francetelecom/clara/cloud/commons/testconfigurations/diff/credentials-development_specific.properties)
   * credentials are loaded from the classpath. By default, [credentials-reference.properties](https://github.com/Orange-OpenSource/elpaaso-core/blob/faa5578e136cd81a31ad7a2b711f5db655b3fe9f/cloud-commons-parent/cloud-commons-test-configurations/src/main/resources/com/francetelecom/clara/cloud/commons/testconfigurations/credentials-reference.properties) is loaded.
   * To load a different properties file, define the `datacenter` property, e.g. '-Ddatacenter=public' will load credentials-public.properties from the classpath.
   * Orange private ci triggers the loading of private credentials by enabling the [jenkins-orange profile](https://github.com/Orange-OpenSource/elpaaso-core/blob/497f71b3c396c7e879334a15fcdaee2220b92a92/pom.xml#L1241-L1258)
* Acceptance tests (AT) are launched through the ``runATs`` maven target
  * ATs run tests against a deployed elpaaso instance through web UI tests and SOAP API tests.

# Deployment on CF

See support config in [credentials-reference.properties](https://github.com/Orange-OpenSource/elpaaso-core/blob/faa5578e136cd81a31ad7a2b711f5db655b3fe9f/cloud-commons-parent/cloud-commons-test-configurations/src/main/resources/com/francetelecom/clara/cloud/commons/testconfigurations/credentials-reference.properties)
