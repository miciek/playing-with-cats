name := "playing-with-cats"

organization := "miciek"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

resolvers += Resolver.jcenterRepo

libraryDependencies ++= {
  val catsVersion = "0.7.2"
  val akkaVersion = "2.4.10"
  val typesafeConfigVersion = "1.3.0"
  val scalaTestVersion = "3.0.0"
  val junitVersion = "4.12"
  Seq(
    "org.typelevel" %% "cats" % catsVersion,
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe" % "config" % typesafeConfigVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
    "junit" % "junit" % junitVersion % Test
  )
}

fork := true
