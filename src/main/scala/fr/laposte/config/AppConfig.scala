package fr.laposte.config

object AppConfig {
  object Kafka {
    val bootstrapServers: String = "localhost:9092"
    val schemaRegistryUrl: String = "http://localhost:8081"
    
    object Topics {
      val shipmentEvents: String = "shipment-events"
      val locationReference: String = "location-reference"
      val enrichedShipmentEvents: String = "enriched-shipment-events"
    }
    
    object Consumer {
      val groupId: String = "logistics-processor"
    }
  }
}