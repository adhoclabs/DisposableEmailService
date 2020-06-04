name := "microservice_template"

version := "0.1"

scalaVersion := "2.12.10"

libraryDependencies ++= Seq(
  // External dependencies
  "ch.qos.logback"      %  "logback-classic"                  % "1.2.3",
  "com.typesafe.akka"   %% "akka-actor"                       % "2.6.5",
  "com.typesafe.akka"   %% "akka-stream"                      % "2.6.5",
  "com.typesafe.akka"   %% "akka-http"                        % "10.1.12",
  "com.typesafe.akka"   %% "akka-http-spray-json"             % "10.1.12",
  "org.json4s"          %% "json4s-jackson"                   % "3.6.1",
  "org.postgresql"      %  "postgresql"                       % "42.2.10",
  "com.typesafe.slick"  %% "slick"                            % "3.3.2",
  "com.typesafe.slick"  %% "slick-hikaricp"                   % "3.3.2",
  "com.github.tminglei" %% "slick-pg"                         % "0.18.1",
  "org.flywaydb"        %  "flyway-core"                      % "6.3.0",

  // Our dependencies
  "co.adhoclabs"        %% "model"                            % "1.12.39",
  "co.adhoclabs"        %% "analytics"                        % "1.0.5",

  // Test dependencies
  "org.scalatest"       %% "scalatest"                        % "3.1.2"          % Test,
  "org.scalamock"       %% "scalamock"                        % "4.4.0"          % Test,
  "com.typesafe.akka"   %% "akka-stream-testkit"              % "2.6.5"          % Test,
  "com.typesafe.akka"   %% "akka-http-testkit"                % "10.1.11"        % Test
)

// Prevents tests from executing when running 'sbt assembly' (prevents repetition in Circle)
test in assembly := {}

// Otherwise we get no logging from tests on secondary threads
parallelExecution in Test := false

// Use ScalaTest's log buffering to see test logs in the correct order
logBuffered in Test := false
