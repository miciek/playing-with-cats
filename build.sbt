name := "playing-with-cats"

organization := "miciek"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

resolvers += Resolver.jcenterRepo

libraryDependencies ++= {
  val catsVersion = "0.9.0"
  val akkaVersion = "2.4.17"
  val typesafeConfigVersion = "1.3.1"
  val scalaTestVersion = "3.0.1"
  val junitVersion = "4.12"
  Seq(
    "org.typelevel" %% "cats" % catsVersion,
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe" % "config" % typesafeConfigVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
    "junit" % "junit" % junitVersion % Test,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
  )
}

fork := true

import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

SbtScalariform.scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignParameters, true)
  .setPreference(AlignArguments, true)
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(DanglingCloseParenthesis, Preserve)
  .setPreference(RewriteArrowSymbols, true)
