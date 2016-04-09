import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

name := "raft"

organization := "com.art4ul"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= {
  val akkaVersion = "2.3.9"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    //"com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    //"com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
    "com.typesafe" % "config" % "1.3.0"
  )}

PB.protobufSettings

PB.flatPackage in PB.protobufConfig := true

PB.generatedTargets in PB.protobufConfig :=
  Seq((sourceDirectory.value / "generated" / "scala", "*.scala"))

PB.runProtoc in PB.protobufConfig := (args =>
  com.github.os72.protocjar.Protoc.runProtoc("-v261" +: args.toArray))

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

mainClass in assembly := Some("com.art4ul.raft.Main")

artifact in (Compile, assembly) := {
  val art = (artifact in (Compile, assembly)).value
  art.copy(`classifier` = Some("assembly"))
}

//assemblyMergeStrategy in assembly := {
//  case PathList (ps @ _*) if ps.last == "pom.properties" => MergeStrategy.discard
//  case PathList ("META-INF", "MANIFEST.MF") => MergeStrategy.discard
//  case PathList (ps @ _*) if ps.last endsWith  ".pom" => MergeStrategy.discard
//  case PathList (ps @ _*) if ps.last == "pom.xml" => MergeStrategy.discard
//  case x =>
//    val oldStrategy = (assemblyMergeStrategy in assembly).value
//    oldStrategy(x)
//}

