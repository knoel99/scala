package fr.laposte

package object models {
  case class KafkaConfig(
    bootstrapServers: String,
    schemaRegistryUrl: String,
    groupId: String
  )

  object KafkaConfig {
    def default: KafkaConfig = KafkaConfig(
      bootstrapServers = "localhost:9092",
      schemaRegistryUrl = "http://localhost:8081",
      groupId = "logistics-processor"
    )
  }
}