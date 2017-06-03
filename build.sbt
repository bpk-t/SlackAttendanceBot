name := "SlackAttendanceBot"

version := "0.0.1"
scalaVersion := "2.12.2"

libraryDependencies += "com.github.gilbertw1" %% "slack-scala-client" % "0.2.1"
libraryDependencies += "com.h2database" % "h2" % "1.4.195"
libraryDependencies += "io.getquill" % "quill-jdbc_2.12" % "1.2.1"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "commons-daemon" % "commons-daemon" % "1.0.15"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

libraryDependencies += "com.typesafe" % "config" % "1.3.1"
libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.13"

flywayUrl := "jdbc:h2:file:./attendance"
flywayUser := "test_user"
    