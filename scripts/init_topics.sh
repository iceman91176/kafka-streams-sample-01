curl -X POST \
  http://schema-registry:8081/subjects/generic-location-value/versions \
  -H 'Cache-Control: no-cache' \
  -H 'Content-Type: application/vnd.schemaregistry.v1+json' \
  -d '{"schema":"[ { \"type\": \"record\", \"name\": \"GenericLocation\", \"namespace\": \"de.witcom.test.schema.avro.location\", \"fields\": [ { \"name\": \"loc_id\", \"type\": \"string\" }, { \"name\": \"name\", \"type\": \"string\" } ] } ]"}'