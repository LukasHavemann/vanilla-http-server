package de.havemann.lukas.vanillahttp

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.request.builder.HttpRequestBuilder.toActionBuilder

import scala.concurrent.duration.DurationInt

//noinspection TypeAnnotation
class SimpleVanillaRequestSimulation extends Simulation {

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl("http://localhost:8080")
    .shareConnections

  val textFileRequest = scenario("request file")
    .exec(toActionBuilder(http("request text file")
      .get("/fileonfirstlevel.txt")
      .check(status.is(200))
      .check(substring("file on first level").exists)))

  setUp(
    textFileRequest.inject(
      rampUsers(20).during(15.seconds),
      constantUsersPerSec(100).during(30.seconds))
      .protocols(httpProtocol))
}