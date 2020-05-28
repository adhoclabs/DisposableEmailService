name := "microservice_template"

version := "0.1"

scalaVersion := "2.12.10"

libraryDependencies ++= Seq(
  "com.typesafe.akka"   %% "akka-actor"                       % "2.6.5",
  "com.typesafe.akka"   %% "akka-stream"                      % "2.6.5",
  "com.typesafe.akka"   %% "akka-http"                        % "10.1.12",
  "com.typesafe.akka"   %% "akka-http-spray-json"             % "10.1.12",
  "ch.qos.logback"      %  "logback-classic"                  % "1.2.3",
  "org.flywaydb"        %  "flyway-core"                      % "6.3.0",
  "org.postgresql"      %  "postgresql"                       % "42.2.10",
  "com.typesafe.slick"  %% "slick"                            % "3.3.2",
  "com.typesafe.slick"  %% "slick-hikaricp"                   % "3.3.2",
  "com.github.tminglei" %% "slick-pg"                         % "0.18.1",
  "org.json4s"          %% "json4s-jackson"                   % "3.6.1",
  "org.scalactic"       %% "scalactic"                        % "3.1.1",
  "co.adhoclabs"        %% "analytics"                        % "1.0.5",
  "org.scalatest"       %% "scalatest"                        % "3.1.1"          % Test,
  "org.scalamock"       %% "scalamock"                        % "4.4.0"          % Test,
  "com.typesafe.akka"   %% "akka-stream-testkit"              % "2.6.5"          % Test,
  "com.typesafe.akka"   %% "akka-http-testkit"                % "10.1.11"        % Test
)

test in assembly := {}