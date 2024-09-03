name := """social-network"""
organization := "novalite"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.14"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test
libraryDependencies += "org.playframework" %% "play-slick" % "6.1.0"
libraryDependencies += "mysql" % "mysql-connector-java" % "8.0.27"
libraryDependencies += "org.mindrot" % "jbcrypt" % "0.4"
libraryDependencies += "com.pauldijou" %% "jwt-play" % "5.0.0"
libraryDependencies += "com.pauldijou" %% "jwt-core" % "5.0.0"
libraryDependencies += "com.auth0" % "jwks-rsa" % "0.6.1"
libraryDependencies += "com.typesafe.play" %% "twirl-api" % "1.5.1"

libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.EarlySemVer
dependencyOverrides += "org.scala-lang.modules" %% "scala-xml" % "2.2.0"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "novalite.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "novalite.binders._"
