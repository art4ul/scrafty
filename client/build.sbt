import sbtassembly.AssemblyPlugin.autoImport._

name := "client"

organization := "com.art4ul.scrafty"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.8"

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

mainClass in assembly := Some("com.art4ul.raft.Main")

artifact in (Compile, assembly) := {
  val art = (artifact in (Compile, assembly)).value
  art.copy(`classifier` = Some("assembly"))
}