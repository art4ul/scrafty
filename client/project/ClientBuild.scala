import sbt._

object ClientBuild extends Build {
  lazy val common = ProjectRef(file("../common"), "common")
  val client = Project("client", file(".")).dependsOn(common)
}


