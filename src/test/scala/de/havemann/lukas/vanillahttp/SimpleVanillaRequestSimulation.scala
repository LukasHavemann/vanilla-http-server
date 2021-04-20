package de.havemann.lukas.vanillahttp

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.request.builder.HttpRequestBuilder.toActionBuilder

import scala.concurrent.duration.DurationInt

/**
 * Simple simulation to load a file from vanilla-http-server without caching of file due to ETag
 */
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
      rampUsersPerSec(10).to(2000).during(10.seconds),
      constantUsersPerSec(3000).during(30.seconds))
      .protocols(httpProtocol))
}