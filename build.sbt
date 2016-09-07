name := "playing-with-cats"

organization := "miciek"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

resolvers += Resolver.jcenterRepo

libraryDependencies ++= {
  val catsVersion = "0.7.2"
  val scalaTestVersion = "3.0.0"
  val junitVersion = "4.12"
  Seq(
    "org.typelevel" %% "cats" % catsVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
    "junit" % "junit" % junitVersion % Test
  )
}

fork := true
