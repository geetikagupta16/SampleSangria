name := "SampleSangria"
 
version := "1.0" 
      
lazy val `samplesangria` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice )
libraryDependencies ++= Seq("org.sangria-graphql" %% "sangria" % "1.4.2" ,
  "org.sangria-graphql" %% "sangria-play-json" % "1.0.4")


unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

      