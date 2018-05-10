import sbt.Keys.scalaVersion

resolvers += Classpaths.typesafeReleases

val Specs2Version = "4.0.2"
val LogbackVersion = "1.2.3"
val CirceVersion = "0.9.1"
val scalaJSReactVersion = "1.2.0"
val scalaCssVersion = "0.5.5"
val reactJSVersion = "15.6.2"
val ScalatraVersion = "2.5.4"
val JettyVersion = "9.2.22.v20170606"
val PrickleVersion = "1.1.14"

lazy val commonSettings = Seq(
  version := "0.0.1-SNAPSHOT",
  organization := "com.sky.ukiss",
  scalaVersion := "2.12.4",
  test in assembly := {},
)

lazy val common = crossProject.crossType(CrossType.Pure)
lazy val commonJS = common.js.settings(
  libraryDependencies ++= Seq(
    "com.github.benhutchison" %%% "prickle" % PrickleVersion,
  )
)
lazy val commonJVM = common.jvm.settings(
  libraryDependencies ++= Seq(
    "com.github.benhutchison" %% "prickle" % PrickleVersion,
  )
)

lazy val backend = project.settings(
  commonSettings,
  mainClass in assembly := Some("com.sky.ukiss.pipelinespawner.Server"),
  assemblyJarName in assembly := s"pipeline-spawner-${version.value}.jar",
  unmanagedResourceDirectories in Compile += baseDirectory.value / ".." / "static",
  libraryDependencies ++= Seq(
    "org.scalatra" %% "scalatra" % ScalatraVersion,
    "org.scalatra" %% "scalatra-scalate" % ScalatraVersion,
    "org.scalatra" %% "scalatra-atmosphere" % ScalatraVersion,
    "org.scalatra" %% "scalatra-json" % ScalatraVersion,
    "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % Test,
    "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % Test,
    "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",
    "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
//    "io.circe" %% "circe-generic" % CirceVersion,
//    "io.circe" %% "circe-literal" % CirceVersion,
//    "io.circe" %% "circe-parser" % CirceVersion,
    "org.json4s" %% "json4s-jackson" % "3.5.2",
    "org.log4s" %% "log4s" % "1.4.0",
    "org.specs2" %% "specs2-core" % Specs2Version % "test",
    "org.specs2" %% "specs2-mock" % Specs2Version % "test",
    "com.eed3si9n" %% "gigahorse-github" % "gigahorse0.3.1_0.2.0",
    "ch.qos.logback" % "logback-classic" % LogbackVersion,
    "io.fabric8" % "kubernetes-client" % "3.1.8",
    "org.eclipse.jetty" % "jetty-plus" % JettyVersion % "container;provided",
    "org.eclipse.jetty" % "jetty-webapp" % JettyVersion,
    "org.eclipse.jetty" % "jetty-continuation" % JettyVersion,
    "org.eclipse.jetty.websocket" % "websocket-server" % JettyVersion,
    "javax.servlet" % "javax.servlet-api" % "3.1.0" % "container;provided;test" artifacts Artifact("javax.servlet-api", "jar", "jar")

  )
).dependsOn(commonJVM)
  .enablePlugins(SbtTwirl)
  .enablePlugins(ScalatraPlugin)

lazy val frontend = project
  .settings(
    commonSettings,
    scalaJSUseMainModuleInitializer := true,
    Seq(fullOptJS, fastOptJS, packageJSDependencies, packageMinifiedJSDependencies)
      .map(task => crossTarget in(Compile, task) := file("static/content/target")),
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.5",
      "com.github.japgolly.scalajs-react" %%% "core" % scalaJSReactVersion,
      "com.github.japgolly.scalajs-react" %%% "extra" % scalaJSReactVersion,
      "com.github.japgolly.scalacss" %%% "core" % scalaCssVersion,
      "com.github.japgolly.scalacss" %%% "ext-react" % scalaCssVersion,
//      "io.circe" %%% "circe-parser" % CirceVersion,
//      "io.circe" %%% "circe-generic" % CirceVersion
    ),
    jsDependencies ++= Seq(
      "org.webjars.npm" % "react" % reactJSVersion / "react-with-addons.js" commonJSName "React" minified "react-with-addons.min.js",
      "org.webjars.npm" % "react-dom" % reactJSVersion / "react-dom.js" commonJSName "ReactDOM" minified "react-dom.min.js" dependsOn "react-with-addons.js"
    ),
    skip in packageJSDependencies := false
  )
  .dependsOn(commonJS)
  .enablePlugins(ScalaJSPlugin)