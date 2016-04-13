import sbt.Keys._
import sbt._

object AdServingSegmentBuild extends Build {
  lazy val common = ProjectRef(file("common"), "common")
  lazy val server = ProjectRef(file("scrafty-server"), "scrafty-server")
  lazy val client = ProjectRef(file("scrafty-client"), "scrafty-client")

  lazy val root = (project in file(".")).aggregate(common, server, client)
}


