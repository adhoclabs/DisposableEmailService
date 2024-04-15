package co.adhoclabs.template.models

import zio.schema.{DeriveSchema, Schema}

import java.net.{Inet4Address, InetAddress}

case class BurnerEmailAddress(address: String)

object BurnerEmailAddress {

  implicit val schema: Schema[BurnerEmailAddress] = DeriveSchema.gen
}
