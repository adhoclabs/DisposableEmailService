package co.adhoclabs.template.api

import zio._
import zio.http._
import zio.http.codec.PathCodec
import zio.http.endpoint.openapi.{OpenAPIGen, SwaggerUI}
import zio.http.endpoint.Endpoint

object FallbackRoute {
  val fallback =
    Endpoint(Method.ANY / PathCodec.trailing)
      .out[String]

  val endpoints =
    List(
      fallback
    )

  val fallbackRoute =
    fallback.implement {
      Handler.fromFunction { (p: Path) =>
        println("Fallback route")
        "Miss me with that shit: " + p
      }
    }

  val routes = Routes(fallbackRoute)

}

/*
Routes(
            Method.ANY / PathCodec.trailing -> handler { (_: Path, req: Request) =>
              Response.text(req.url.encode)
            },
          )
 */
