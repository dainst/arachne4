# Arachne 4 Backend

for documentation see http://dai-softsource.uni-koeln.de/projects/be/wiki 

## Building

Before building, make sure you have a `Java 8 SDK` and `maven` installed on your machine.

### Full build
This builds the servlet as .war file and runs the unit as well as the integration tests. (The build machine must be able to connect to the needed services for example the DB, which may require VPN access to the UoC Network)
<pre>
mvn clean integration-test
</pre>

### Package
This builds the servlet as .war file and runs the unit tests. (No special requirements for the build machine)
<pre>
mvn clean package
</pre>


### Unit Test
This compiles the servlet and runs the unit tests. (No special requirements for the build machine)
<pre>
mvn clean test
</pre>


