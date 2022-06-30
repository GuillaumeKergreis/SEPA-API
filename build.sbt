name := "scala-api"

version := "0.1"

scalaVersion := "2.13.8"

libraryDependencies += "org.typelevel" %% "cats-core" % "2.8.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "3.3.12"

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

val http4sVersion = "0.23.12"
libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion
)

val tapirVersion = "1.0.1"
libraryDependencies ++= Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-client" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-cats" % tapirVersion
)

lazy val doobieVersion = "1.0.0-RC2"
libraryDependencies ++= Seq(
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion
)

lazy val scalatestVersion = "3.2.12"
libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % scalatestVersion,
  "org.scalatest" %% "scalatest" % scalatestVersion % "test"
)


lazy val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "2.0.1"
lazy val scalaParser = "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1"
//lazy val dispatchV = "0.13.4"
//lazy val dispatch = "net.databinder.dispatch" % "dispatch-core" % dispatchV

enablePlugins(ScalaxbPlugin)

//libraryDependencies ++= Seq(dispatch)
libraryDependencies ++= Seq(scalaXml, scalaParser)

//Compile / scalaxb / scalaxbDispatchVersion += dispatchV
Compile / scalaxb / scalaxbPackageName := "generated"
// scalaxbPackageNames in (Compile, scalaxb)    := Map(uri("http://schemas.microsoft.com/2003/10/Serialization/") -> "microsoft.serialization"),
// logLevel in (Compile, scalaxb) := Level.Debug
