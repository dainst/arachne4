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

You need to create the folder `/var/log/arachne/arachnedataservice/` and make it writable
before you can run the server.

If you have to change the `log4j2.xml`, because you don't develop on Linux, the original content is still available in
`log4j2.xml.org`. Copy and paste its contents back in `log4j2.xml` in case needed.

Run the server with

```
mvn tomcat7:run
```

You then should be able to access the backend under `localhost:8080/arachne/entity/1`.
Alternatively you can use the Servlet container provided by your IDE in order to have a more seamless developing experience.
