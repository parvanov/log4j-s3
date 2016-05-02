name := "log4j-s3"
version := "0.0.4"
organization := "com.log4js3"
description := "Log4j Appender to transfer to AWS S3"
licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))
homepage := Some(url("https://github.com/pomadchin/log4j-s3"))
publishMavenStyle := true
bintrayRepository := "maven"
bintrayOrganization := None
crossPaths := false

libraryDependencies ++= Seq(
  "log4j"         % "log4j"           % "1.2.17",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.9.34"
)

mappings in (Compile, packageBin) ~= { (ms: Seq[(File, String)]) =>
  ms filter { case (file, toPath) =>
    toPath != "com/log4js3/example/Main.class"
  }
}
