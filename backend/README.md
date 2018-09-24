# Arachne 4 Backend

for documentation see http://dai-softsource.uni-koeln.de/projects/be/wiki 

## Prerequisites

In order to develop and/or build the application, a configuration file ist necessary. 
You can take the one marked for development on Github found [here](https://github.com/dainst/arachne-configs/tree/master/arachne4).

It should be placed at

```
src/main/resources/config/application.properties
``` 

Before building or running the backend, 
make sure you have a `Java 8 SDK` and `maven` installed on your machine.

### image cache dir
In the config-file, you find a parameter called "imageCacheDir". You need this directory to be existing.

## Building

### Full build
This builds the servlet as .war file and runs the unit as well as the integration tests. (The build machine must be able to connect to the needed services for example the DB, which may require VPN access to the UoC Network)

```
mvn clean integration-test
```

### Package
This builds the servlet as .war file and runs the unit tests. (No special requirements for the build machine)


```
mvn clean package
``` 


### Unit Test
This compiles the servlet and runs the unit tests. (No special requirements for the build machine)

```
mvn clean test
```

## Development

### MySQL mixed-case table names not supported by OS

Some operating systems do not support mixed case MySQL table names properly but Arachne does contain such tables and therefor the unit tests need to account for that.
The backend should not be deployed on such an OS but you can use it for development if you define the Java system property `myMachineDoesNotSupportMixedCaseSQLTableNames`.

For example:
```
mvn -DmyMachineDoesNotSupportMixedCaseSQLTableNames test
```

Remember to also add this system property to the run- an debug-configurations of your IDE of choice.

### Developing without transl8 access

Add the property
```
transl8enabled=false
```
to your local *application.properties* file.
(It is not needed to set this property in production as it defaults to true).
### Logging

You need to create the folder `/var/log/arachne/arachnedataservice/` and make it writable
before you can run the server.

If you have to change the `log4j2.xml`, because you don't develop on Linux, the original content is still available in
`log4j2.xml.org`. Copy and paste its contents back in `log4j2.xml` in case needed.

### Running

Run the server with

```
mvn tomcat7:run
```

You then should be able to access the backend under `localhost:8080/data/entity/1`.
Alternatively you can use the Servlet container provided by your IDE in order to have a more seamless developing experience.


