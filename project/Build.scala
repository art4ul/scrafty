import sbt._

object AdServingSegmentBuild extends Build {
  lazy val common = ProjectRef(file("common"), "common")
  lazy val core = ProjectRef(file("core"), "core")
  lazy val client = ProjectRef(file("client"), "client")

  lazy val root = (project in file(".")).aggregate(common, core, client)
}


