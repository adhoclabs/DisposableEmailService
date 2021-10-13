organization := "co.adhoclabs"

name := "microservice_template"

version := "0.1"

scalaVersion := "2.12.12"

val akkaVersion = "2.6.16"
val akkaHttpVersion = "10.2.6"

libraryDependencies ++= Seq(
  // External dependencies
  "ch.qos.logback"      %  "logback-classic"      % "1.2.3",
  "com.typesafe.akka"   %% "akka-actor"           % akkaVersion,
  "com.typesafe.akka"   %% "akka-stream"          % akkaVersion,
  "com.typesafe.akka"   %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka"   %% "akka-http-spray-json" % akkaHttpVersion,
  "org.postgresql"      %  "postgresql"           % "42.2.24",
  "com.typesafe.slick"  %% "slick"                % "3.3.3",
  "com.typesafe.slick"  %% "slick-hikaricp"       % "3.3.3",
  "com.github.tminglei" %% "slick-pg"             % "0.19.7",
  "org.flywaydb"        %  "flyway-core"          % "7.15.0",

  // Our dependencies
  "co.adhoclabs" %% "analytics" % "1.0.8",
  "co.adhoclabs" %% "model"     % "2.2.5",
  "co.adhoclabs" %% "secrets"   % "1.0.0",

  // Test dependencies
  "org.scalatest"     %% "scalatest"           % "3.2.10"        % Test,
  "org.scalamock"     %% "scalamock"           % "5.1.0"         % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion     % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion     % Test,
  "com.typesafe.akka" %% "akka-http-testkit"   % akkaHttpVersion % Test
)

// Prevents tests from executing when running 'sbt assembly' (prevents repetition in Circle)
test in assembly := {}

// Otherwise we get no logging from tests on secondary threads
parallelExecution in Test := false

// Use ScalaTest's log buffering to see test logs in the correct order
logBuffered in Test := false

// If running `sbt assembly` results in an error message containing:
//   java.lang.RuntimeException: deduplicate: different file contents found in the following:
// then implement a merge strategy like the one below (See https://github.com/sbt/sbt-assembly#merge-strategy for information):
//assemblyMergeStrategy in assembly := {
//  case PathList("reference.conf") => MergeStrategy.concat
//  case PathList("META-INF", _*) => MergeStrategy.discard
//  case _ => MergeStrategy.first
//}
