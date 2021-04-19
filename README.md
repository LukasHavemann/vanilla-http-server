# :icecream: vanilla-http-server

Simple but full functional implementation of a http-server without a http framework in vanilla
java. [Why vanilla?](https://thisinterestsme.com/vanilla-javascript/)

## :whale2: How to launch?

The vanilla-http-server can be started as a docker container with the following command. The container will start
serving files form a sample directory inside the docker image.

```
 docker run -d -p 8080:8080 lukashavemann/vanilla-http-server:latest
```

To serve files from the host, you can us a [bind mount](https://docs.docker.com/storage/bind-mounts/). In the following
example the vanilla-http-server will serve files from the current working dir.

```
 docker run -d -p 8080:8080 --mount type=bind,source="$(pwd)",target=/basedir lukashavemann/vanilla-http-server:latest
```

If you want to override the configuration defaults of the vanilla-http-server instance, you can pass confiuration
properties as command line arguments. The following examples increases the http connection keep-alive-timeout to
25s. [See here](/src/main/resources/default-application.yml) for a full documentation of possible properties.

````
docker run -d -p 8080:8080 lukashavemann/vanilla-http-server:latest --vanilla.server.http.keepAliveTimeout=25s
````

## :package: Dependencies

The project uses spring boot starter for dependency injection and configuration management. The web context of spring
boot **[is disabled](src/main/java/de/havemann/lukas/vanillahttp/VanillaHttpServer.java)**. The http protocol
implementation ist build from scratch. The framework jsoup is used for automated acceptance testing.

## :house: Architecture

## :test_tube: Unittests & Acceptancetest

Critical components are tested with unit tests. All requirements from the technical assessment are validated with
automated end-to-end acceptance test.

* [Basic Reguirements Acceptancetest](src/test/java/de/havemann/lukas/vanillahttp/acceptancetest/BasicRequirementsAcceptanceTest.java)
* [Extension 1 Acceptancetest](src/test/java/de/havemann/lukas/vanillahttp/acceptancetest/Extension1AcceptanceTest.java)
* [Extension 2 Acceptancetest](src/test/java/de/havemann/lukas/vanillahttp/acceptancetest/Extension2AcceptanceTest.java)

## :gun: Loadtesting

To validate that the implementation can serve multiple concurrent request, a simple [Gatling](https://gatling.io/)
loadtest scenario [was implemented](src/test/scala/de/havemann/lukas/vanillahttp/SimpleVanillaRequestSimulation.scala).
The scenario can be started with ```mvn gatling:test```. On a Macbook Air M1, 2020, 16GB, macOS big sur 11.2 the
following loadtest result could be achieved.

## :hammer: Build Process & Pipeline

To build and run the project locally.

```
mvn clean package
java -jar target/vanilla-http-server-1.0-SNAPSHOT.jar
```

The execution of the tests was automated
with [github actions](https://github.com/LukasHavemann/vanilla-http-server/actions). The docker image build was
automated with [docker hub](https://hub.docker.com/repository/docker/lukashavemann/vanilla-http-server).

## :book: Used Online Resources

During development the following online resources were used.

- [maven github actions setup](https://docs.github.com/en/actions/guides/building-and-testing-java-with-maven)
- [jetbrains gitignore](https://github.com/github/gitignore/blob/master/Global/JetBrains.gitignore)
- [maven gitignore](https://github.com/github/gitignore/blob/master/Maven.gitignore)
- [docker github actions setup](https://github.com/marketplace/actions/build-and-push-docker-images)
- [switch to multi stage build](https://stackoverflow.com/questions/61388905/github-action-to-maven-build-followed-by-docker-build-push)
- [switch to layered jar](https://spring.io/blog/2020/01/27/creating-docker-images-with-spring-boot-2-3-0-m1)