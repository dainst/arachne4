FROM maven:3-openjdk-8 as builder

COPY . /backend
WORKDIR /backend

RUN mvn package -DskipTests

FROM tomcat:9-jre8-openjdk

COPY --from=builder /backend/target/data.war /usr/local/tomcat/webapps/

EXPOSE 8080
