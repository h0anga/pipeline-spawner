import sbt.Keys.scalaVersion

val Http4sVersion = "0.18.0"
val Specs2Version = "4.0.2"
val LogbackVersion = "1.2.3"
val CirceVersion = "0.9.1"

lazy val commonSettings = Seq(
  version := "0.0.1-SNAPSHOT",
  organization := "com.sky.ukiss",
  scalaVersion := "2.12.4",
)

lazy val common = crossProject.crossType(CrossType.Pure)
lazy val commonJS = common.js
lazy val commonJVM = common.jvm

lazy val backend = project.settings(
  commonSettings,
  unmanagedResourceDirectories in Compile += baseDirectory.value / ".." / "static",
  libraryDependencies ++= Seq(
    "org.http4s"      %%  "http4s-blaze-server" % Http4sVersion,
    "org.http4s"      %%  "http4s-circe"        % Http4sVersion,
    "org.http4s"      %%  "http4s-dsl"          % Http4sVersion,
    "io.circe"        %%  "circe-generic"       % CirceVersion,
    "io.circe"        %%  "circe-literal"       % CirceVersion,
    "io.circe"        %%  "circe-parser"        % CirceVersion,
    "org.log4s"       %%  "log4s"               % "1.4.0",
    "org.specs2"      %%  "specs2-core"         % Specs2Version % "test",
    "org.specs2"      %%  "specs2-mock"         % Specs2Version % "test",
    "com.eed3si9n"    %%  "gigahorse-github"    % "gigahorse0.3.1_0.2.0",
    "ch.qos.logback"  %   "logback-classic"     % LogbackVersion,
    "io.fabric8"      %   "kubernetes-client"   % "3.1.8"
  )
).dependsOn(commonJVM)

lazy val frontend = project
  .settings(
    commonSettings,
    scalaJSUseMainModuleInitializer := true,
    Seq(fullOptJS, fastOptJS, packageJSDependencies, packageMinifiedJSDependencies)
        .map(task => crossTarget in (Compile, task) := file("static/content/target")),
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.5"
  )
  .dependsOn(commonJS)
  .enablePlugins(ScalaJSPlugin)