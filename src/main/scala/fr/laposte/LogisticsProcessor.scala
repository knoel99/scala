package fr.laposte

import io.confluent.kafka.serializers.{AbstractKafkaSchemaSerDeConfig, KafkaAvroDeserializer, KafkaAvroSerializer}
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import com.typesafe.scalalogging.LazyLogging
import fr.laposte.models.{ShipmentEvent, LocationInfo, EnrichedShipmentEvent}
import com.sksamuel.avro4s._
import config.AppConfig
import scala.collection.JavaConverters._
import java.time.Duration

object LogisticsProcessor extends LazyLogging {
  def processMessages(): Unit = {
    // Configure Consumer
    val consumerProps = new java.util.Properties()
    consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.Kafka.bootstrapServers)
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, AppConfig.Kafka.Consumer.groupId)
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[KafkaAvroDeserializer])
    consumerProps.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, AppConfig.Kafka.schemaRegistryUrl)
    consumerProps.put("specific.avro.reader", "true")

    // Configure Producer
    val producerProps = new java.util.Properties()
    producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.Kafka.bootstrapServers)
    producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
    producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[KafkaAvroSerializer])
    producerProps.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, AppConfig.Kafka.schemaRegistryUrl)

    // Create consumer and producer instances
    val consumer = new KafkaConsumer[String, ShipmentEvent](consumerProps)
    val locationConsumer = new KafkaConsumer[String, LocationInfo](consumerProps)
    val producer = new KafkaProducer[String, EnrichedShipmentEvent](producerProps)

    // Subscribe to topics
    consumer.subscribe(java.util.Arrays.asList(AppConfig.Kafka.Topics.shipmentEvents))
    locationConsumer.subscribe(java.util.Arrays.asList(AppConfig.Kafka.Topics.locationReference))

    // Location cache
    var locationMap = Map[String, LocationInfo]()

    def updateLocationMap(): Unit = {
      val records = locationConsumer.poll(Duration.ofMillis(100))
      records.asScala.foreach { record =>
        locationMap += (record.key -> record.value)
      }
    }

    try {
      while (true) {
        // Update location reference data
        updateLocationMap()

        // Process shipment events
        val records = consumer.poll(Duration.ofMillis(100))
        records.asScala.foreach { record =>
          val shipmentEvent = record.value
          locationMap.get(shipmentEvent.locationId) match {
            case Some(locationInfo) =>
              val enrichedEvent = EnrichedShipmentEvent(
                shipmentId = shipmentEvent.shipmentId,
                timestamp = shipmentEvent.timestamp,
                locationId = shipmentEvent.locationId,
                locationName = locationInfo.name,
                country = locationInfo.country,
                timezone = locationInfo.timezone,
                status = shipmentEvent.status
              )

              producer.send(new ProducerRecord[String, EnrichedShipmentEvent](
                AppConfig.Kafka.Topics.enrichedShipmentEvents,
                shipmentEvent.shipmentId,
                enrichedEvent
              ))

              logger.info(s"Processed shipment ${shipmentEvent.shipmentId}")

            case None =>
              logger.warn(s"Location info not found for ID: ${shipmentEvent.locationId}")
          }
        }
      }
    } catch {
      case e: Exception =>
        logger.error("Error in processing", e)
    } finally {
      consumer.close()
      locationConsumer.close()
      producer.close()
    }
  }
}