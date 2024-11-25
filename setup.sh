#!/bin/bash

docker exec broker kafka-topics \
  --create \
  --bootstrap-server broker:29092 \
  --topic shipment-events \
  --partitions 1 \
  --replication-factor 1

docker exec broker kafka-topics \
  --create \
  --bootstrap-server broker:29092 \
  --topic location-reference \
  --partitions 1 \
  --replication-factor 1 \
  --config cleanup.policy=compact

docker exec broker kafka-topics \
  --create \
  --bootstrap-server broker:29092 \
  --topic enriched-shipment-events \
  --partitions 1 \
  --replication-factor 1