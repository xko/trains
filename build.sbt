name := "wunder-reise"
version := "0.1"

scalaVersion := "2.13.6"

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test"
libraryDependencies += "org.scalatestplus" %% "scalacheck-1-15" % "3.2.9.0" % "test"
