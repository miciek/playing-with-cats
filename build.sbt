name := "playing-with-cats"

organization := "miciek"

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.4"

resolvers += Resolver.jcenterRepo

libraryDependencies ++= {
  val catsV = "1.1.0"
  val akkaV = "2.4.17"
  val configV = "1.3.1"
  val scalatestV = "3.0.1"
  Seq(
    "org.typelevel" %% "cats-core" % catsV,
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe" % "config" % configV,
    "org.scalatest" %% "scalatest" % scalatestV % Test,
    "com.typesafe.akka" %% "akka-testkit" % akkaV % Test
  )
}

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")

scalacOptions ++= Seq(
  "-unchecked",
  "-feature",
  "-Ywarn-unused-import",
  "-Xfatal-warnings",
  "-Ypartial-unification",
  "-language:higherKinds",
  "-Xlint"
)

scalafmtVersion in ThisBuild := "1.3.0"
scalafmtOnCompile in ThisBuild := true

fork := true
connectInput in run := true
