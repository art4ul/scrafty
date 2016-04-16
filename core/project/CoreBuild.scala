import sbt._

object CoreBuild extends Build {
  lazy val common = ProjectRef(file("../common"), "common")
  val core = Project("core", file(".")).dependsOn(common)
}


