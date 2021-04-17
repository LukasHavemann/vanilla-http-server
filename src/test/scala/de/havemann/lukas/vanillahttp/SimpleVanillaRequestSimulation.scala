package de.havemann.lukas.vanillahttp

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration.DurationInt

//noinspection TypeAnnotation
class SimpleVanillaRequestSimulation extends Simulation {

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl("http://localhost:8080")
    .shareConnections

  val textFileRequest = scenario("request file")
    .exec(http("request text file")
      .get("/src/test/resources/sampledirectory/fileonfirstlevel.txt"))

  setUp(
    textFileRequest.inject(
      rampUsers(50).during(15.seconds),
      constantUsersPerSec(200).during(30.seconds))
      .protocols(httpProtocol))
}