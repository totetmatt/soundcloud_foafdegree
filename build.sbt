name := "SoundCloud_FoafDegree"

version := "1.0"

scalaVersion := "2.11.8"

val sparkVersion = "2.0.1"

libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion
libraryDependencies += "junit" % "junit" % "4.10" % "test"

libraryDependencies += "com.github.scopt" %% "scopt" % "3.5.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test"
libraryDependencies += "org.apache.spark" %% "spark-graphx"  % sparkVersion
