#
# Build stage
#
FROM maven:3.8.1-openjdk-11 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

#
# Package stage
#
FROM openjdk:11-jre-slim
COPY --from=build /home/app/target/vanilla-http-server.jar /usr/local/lib/vanilla-http-server.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/vanilla-http-server.jar"]