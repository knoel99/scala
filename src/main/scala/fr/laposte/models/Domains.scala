package fr.laposte.models

case class ShipmentEvent(
  shipmentId: String,
  timestamp: Long,
  locationId: String,
  status: String
)

case class LocationInfo(
  locationId: String,
  name: String,
  country: String,
  timezone: String
)

case class EnrichedShipmentEvent(
  shipmentId: String,
  timestamp: Long,
  locationId: String,
  locationName: String,
  country: String,
  timezone: String,
  status: String
)