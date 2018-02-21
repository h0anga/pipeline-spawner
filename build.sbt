val Http4sVersion = "0.18.0"
val Specs2Version = "4.0.2"
val LogbackVersion = "1.2.3"
val CirceVersion = "0.9.1"

lazy val root = (project in file("."))
  .settings(
    organization := "com.sky.ukiss",
    name := "pipeline-spawner",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.4",
    libraryDependencies ++= Seq(
      "org.http4s"      %%  "http4s-blaze-server" % Http4sVersion,
      "org.http4s"      %%  "http4s-circe"        % Http4sVersion,
      "org.http4s"      %%  "http4s-dsl"          % Http4sVersion,
      "io.circe"        %%  "circe-generic"       % CirceVersion,
      "io.circe"        %%  "circe-literal"       % CirceVersion,
      "org.specs2"      %%  "specs2-core"         % Specs2Version % "test",
      "ch.qos.logback"  %   "logback-classic"     % LogbackVersion,
      "io.fabric8"      %   "kubernetes-client"   % "3.1.8"
    )
  )

