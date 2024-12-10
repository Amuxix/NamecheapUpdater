name := "NamecheapUpdater"

val scala3Version = "3.5.0"
ThisBuild / scalaVersion := scala3Version

// Used for scala fix
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

ThisBuild / scalafixOnCompile := true
ThisBuild / scalafmtOnCompile := true

enablePlugins(JavaAppPackaging, AshScriptPlugin)

ThisBuild / publish / skip                      := true
Universal / javaOptions            ++= Seq(
  "-Dconfig.file=/opt/docker/conf/application.conf",
)

javacOptions  ++= Seq("-Xlint", "-encoding", "UTF-8")
scalacOptions ++= Seq(
  "-explain",              // Explain errors in more detail.
  "-explain-types",        // Explain type errors in more detail.
  "-indent",               // Allow significant indentation.
  "-new-syntax",           // Require `then` and `do` in control expressions.
  "-feature",              // Emit warning and location for usages of features that should be imported explicitly.
  "-source:future",        // better-monadic-for
  "-language:higherKinds", // Allow higher-kinded types
  "-deprecation",          // Emit warning and location for usages of deprecated APIs.
  "-Wunused:all",          // Emit warnings for unused imports, local definitions, explicit parameters implicit,
  // parameters method, parameters
  "-Xcheck-macros",
)

libraryDependencies ++= Seq(
  catsEffect,
  circe,
  circeParser,
  catsRetry,
  log4cats,
  logbackClassic,
  pureconfig,
  pureconfigCE,
  http4sDSL,
  http4sEmberServer,
  http4sEmberClient,
  http4sCirce,
  http4sXml,
  scalaXml,
)

lazy val catsEffect        = "org.typelevel"          %% "cats-effect"            % "3.5.4"
lazy val circe             = "io.circe"               %% "circe-core"             % "0.14.7"
lazy val circeParser       = circe.organization       %% "circe-parser"           % circe.revision
lazy val catsRetry         = "com.github.cb372"       %% "cats-retry"             % "3.1.3"
lazy val log4cats          = "org.typelevel"          %% "log4cats-slf4j"         % "2.7.0"
lazy val logbackClassic    = "ch.qos.logback"          % "logback-classic"        % "1.5.6"
lazy val pureconfig        = "com.github.pureconfig"  %% "pureconfig-core"        % "0.17.7"
lazy val pureconfigCE      = pureconfig.organization  %% "pureconfig-cats-effect" % pureconfig.revision
lazy val http4sDSL         = "org.http4s"             %% "http4s-dsl"             % "1.0.0-M38"
lazy val http4sEmberServer = http4sDSL.organization   %% "http4s-ember-client"    % http4sDSL.revision
lazy val http4sEmberClient = http4sDSL.organization   %% "http4s-ember-server"    % http4sDSL.revision
lazy val http4sCirce       = http4sDSL.organization   %% "http4s-circe"           % http4sDSL.revision
lazy val http4sXml         = http4sDSL.organization   %% "http4s-scala-xml"       % "1.0.0-M38.1"
lazy val scalaXml          = "org.scala-lang.modules" %% "scala-xml"              % "2.3.0"
