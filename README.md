# kafka-streams-sample Project

## Create Kafka Topics
```shell script
kafka-topics --bootstrap-server broker:9092 --create --topic generic-location --replication-factor 1 --partitions 1 --config "cleanup.policy=compact" --config "delete.retention.ms=1000"
kafka-topics --bootstrap-server broker:9092 --create --topic generic-location-contact --replication-factor 1 --partitions 1 --config "cleanup.policy=compact" --config "delete.retention.ms=1000"
kafka-topics --bootstrap-server broker:9092 --create --topic generic-contact --replication-factor 1 --partitions 1 --config "cleanup.policy=compact" --config "delete.retention.ms=1000"
```

## Produce Data

```shell script
docker exec -i schema-registry /usr/bin/kafka-avro-console-producer --topic generic-location --bootstrap-server broker:9092 \
  --property "parse.key=true"\
  --property 'key.schema={"type":"string"}'\
  --property "key.separator=:"\
  --property value.schema="$(< src/main/resources/avro/generic-location.avsc)"
"5":{"de.witcom.test.schema.avro.location.GenericLocation":{"loc_id": "5", "name": "just a name"}}
"6":{"de.witcom.test.schema.avro.location.GenericLocation":{"loc_id": "6", "name": "just another name"}}
```

```shell script
docker exec -i schema-registry /usr/bin/kafka-avro-console-producer --topic generic-contact --bootstrap-server broker:9092   --property "parse.key=true"  --property 'key.schema={"type":"string"}'  --property "key.separator=:"  --property value.schema="$(< src/main/resources/avro/generic-contact.avsc)"
"contact-1":{"de.witcom.test.schema.avro.location.GenericContact":{"contact_id": "contact-1", "name": "WiTCOM","first_name":null}}
"contact-2":{"de.witcom.test.schema.avro.location.GenericContact":{"contact_id": "contact-2", "name": "ESWE","first_name":null}}
"contact-3":{"de.witcom.test.schema.avro.location.GenericContact":{"contact_id": "contact-3", "name": "Test GmbH 2","first_name":null}}
```

```shell script
docker exec -i schema-registry /usr/bin/kafka-avro-console-producer --topic generic-location-contact --bootstrap-server broker:9092   --property "parse.key=true"  --property 'key.schema={"type":"string"}'  --property "key.separator=:"  --property value.schema="$(< src/main/resources/avro/generic-location-contact.avsc)"
"key1":{"de.witcom.test.schema.avro.location.GenericLocationContact":{"id": "key1", "loc_id": "5","contact_id":"contact-1"}}
```


This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/kafka-streams-sample-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Provided Code

### RESTEasy JAX-RS

Easily start your RESTful Web Services

[Related guide section...](https://quarkus.io/guides/getting-started#the-jax-rs-resources)
