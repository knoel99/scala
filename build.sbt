name := "kafka-logistics-processor"
version := "0.1"
scalaVersion := "2.13.10"

Compile / mainClass := Some("fr.laposte.Main")

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % "3.3.1",
  "io.confluent" % "kafka-avro-serializer" % "7.3.0",
  "org.apache.avro" % "avro" % "1.11.0",
  "com.sksamuel.avro4s" %% "avro4s-core" % "4.1.1",
  "com.typesafe" % "config" % "1.4.2",
  "ch.qos.logback" % "logback-classic" % "1.2.11",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"
)

resolvers += "Confluent" at "https://packages.confluent.io/maven/"