schemaname="generic-location" 
sed src/main/resources/avro/$schemaname.avsc -e 's/"/\\"/g' | tr '\n' ' ' | sed '1s/^/{"schema":"/' | sed '$ s/$/"}/' > /tmp/$schemaname.json
curl -X POST \
  http://schema-registry:8081/subjects/$schemaname-value/versions \
  -H 'Cache-Control: no-cache' \
  -H 'Content-Type: application/vnd.schemaregistry.v1+json' \
  --data-binary "@/tmp/$schemaname.json"
 rm -rf /tmp/$schemaname.json

schemaname="generic-location-contact" 
sed src/main/resources/avro/$schemaname.avsc -e 's/"/\\"/g' | tr '\n' ' ' | sed '1s/^/{"schema":"/' | sed '$ s/$/"}/' > /tmp/$schemaname.json
curl -X POST \
  http://schema-registry:8081/subjects/$schemaname-value/versions \
  -H 'Cache-Control: no-cache' \
  -H 'Content-Type: application/vnd.schemaregistry.v1+json' \
  --data-binary "@/tmp/$schemaname.json"
 rm -rf /tmp/$schemaname.json

schemaname="generic-contact" 
sed src/main/resources/avro/$schemaname.avsc -e 's/"/\\"/g' | tr '\n' ' ' | sed '1s/^/{"schema":"/' | sed '$ s/$/"}/' > /tmp/$schemaname.json
curl -X POST \
  http://schema-registry:8081/subjects/$schemaname-value/versions \
  -H 'Cache-Control: no-cache' \
  -H 'Content-Type: application/vnd.schemaregistry.v1+json' \
  --data-binary "@/tmp/$schemaname.json"
 rm -rf /tmp/$schemaname.json