logLevel := Level.Warn

libraryDependencies ++= Seq(
  "com.github.os72" % "protoc-jar" % "2.x.5"
)
addSbtPlugin("com.trueaccord.scalapb" % "sbt-scalapb" % "0.4.20")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.12.0")