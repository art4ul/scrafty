name := "common"

organization := "com.art4ul.scrafty"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies ++= {
  Seq(
    "com.twitter" %% "chill" % "0.8.0",
    "com.typesafe" % "config" % "1.3.0"
  )}