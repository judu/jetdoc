seq(conscriptSettings :_*)

organization := "com.clevercloud"

name := "jetdoc"

version := "0.1.0"

scalaVersion := "2.10.1"

scalacOptions ++= Seq("-feature", "-deprecation")

packageArchetype.java_application

libraryDependencies ++= Seq(
   "net.databinder" %% "unfiltered-netty-server" % "0.7.1",
   "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
   "org.clapper" %% "avsl" % "1.0.1",
	"org.apache.ivy" % "ivy" % "2.3.0",
	"org.apache.httpcomponents" % "httpclient" % "4.3.1",
   "net.databinder" %% "unfiltered-spec" % "0.7.1" % "test"
)

resolvers ++= Seq(
  "jboss repo" at "http://repository.jboss.org/nexus/content/groups/public-jboss/",
  "Central" at "http://repo1.maven.org/maven2/"
)
