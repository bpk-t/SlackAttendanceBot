logLevel := Level.Warn

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")
addSbtPlugin("org.flywaydb" % "flyway-sbt" % "4.2.0")
resolvers += "Flyway" at "https://flywaydb.org/repo"