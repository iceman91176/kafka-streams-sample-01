package de.witcom.tests;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.processor.StateStore;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.witcom.test.schema.avro.location.GenericContact;
import de.witcom.test.schema.avro.location.GenericLocation;
import de.witcom.test.schema.avro.location.GenericLocationContact;
import de.witcom.test.schema.avro.location.GenericLocationContactEnriched;
import de.witcom.tests.model.LocationContactEnrichedDto;
import de.witcom.tests.model.LocationWithContactDto;
import de.witcom.tests.streams.LocationTopologyProducer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.quarkus.test.junit.QuarkusTest;

import org.jboss.logging.Logger;

@QuarkusTest
public class FKTopologyTest {

    private static final Logger LOGGER = Logger.getLogger("FKTopologyTest");

    //@ConfigProperty(name = "kafka.schema.registry.url")
    static String schemaRegistryUrl = "mock://fk-join-test";

    @ConfigProperty(name = "contact.topic")
    String contactTopic;

    @ConfigProperty(name = "location-contact.topic")
    String locationContactTopic;

    @ConfigProperty(name = "location.topic")
    String locationTopic;

    @Inject
    LocationTopologyProducer locationTopology;

    @Test
    public void testJoin() throws IOException {

        final Topology topology = locationTopology.buildTopology();

        try (final TopologyTestDriver driver = new TopologyTestDriver(topology)) {

            Serializer<String> keySerializer = Serdes.String().serializer();
            Deserializer<String> keyDeserializer = Serdes.String().deserializer();
            Serializer<GenericLocationContact> locContactSerializer = buildAvroValueSerde(GenericLocationContact.class).serializer();
            Serializer<GenericContact> contactSerializer = buildAvroValueSerde(GenericContact.class).serializer();
            Serializer<GenericLocation> locationSerializer = buildAvroValueSerde(GenericLocation.class).serializer();
            
            //GenericLocationContactEnriched
            //Deserializer<GenericLocationContactEnriched> locContactEnrichedDeSerializer = buildAvroValueSerde(GenericLocationContactEnriched.class).deserializer();

            //create topics
            //input
            final TestInputTopic<String, GenericLocationContact> locContacts = driver.createInputTopic(locationContactTopic, keySerializer, locContactSerializer);
            final TestInputTopic<String, GenericContact> contacts = driver.createInputTopic(contactTopic, keySerializer, contactSerializer);
            final TestInputTopic<String, GenericLocation> locations = driver.createInputTopic(locationTopic, keySerializer, locationSerializer);
            //output
            //TestOutputTopic<String, GenericLocationContactEnriched> enrichedLocContacts = driver.createOutputTopic("intermediate.topic", keyDeserializer, locContactEnrichedDeSerializer);


            // input some data
            locations.pipeInput("location1", GenericLocation.newBuilder().setLocId("location1").setName("Lokation 1").build());
            locations.pipeInput("location2", GenericLocation.newBuilder().setLocId("location2").setName("Lokation 2").build());
            locations.pipeInput("location3", GenericLocation.newBuilder().setLocId("location3").setName("Lokation 3").build());


            contacts.pipeInput("c1", GenericContact.newBuilder().setContactId("c1").setName("WiTCOM GmbH").setFirstName(null).build());
            contacts.pipeInput("c2", GenericContact.newBuilder().setContactId("c2").setName("ESWE Versorgung").setFirstName(null).build());
            contacts.pipeInput("c3", GenericContact.newBuilder().setContactId("c1").setName("Test GmbH").setFirstName(null).build());
            
            GenericLocationContact.newBuilder().setId("id1").setContactId("c1").setLocId("location1");
            locContacts.pipeInput("id1", GenericLocationContact.newBuilder().setId("id1").setContactId("c1").setLocId("location1").build());
            locContacts.pipeInput("id2", GenericLocationContact.newBuilder().setId("id2").setContactId("c2").setLocId("location1").build());


            KeyValueStore<String, LocationContactEnrichedDto> store = driver.getKeyValueStore("LOC-CONTACT-ENRICHED-MV");
            KeyValueIterator<String, LocationContactEnrichedDto> enriched = store.all();
            while (enriched.hasNext()){
                KeyValue<String, LocationContactEnrichedDto> e = enriched.next();
                LOGGER.infof("key %s, value %s", e.key,e.value.toString());
            }

            KeyValueStore<String, LocationWithContactDto> locStore = driver.getKeyValueStore("LOC-ENRICHED-MV");
            KeyValueIterator<String, LocationWithContactDto> locIterator = locStore.all();
            while (locIterator.hasNext()){
                KeyValue<String, LocationWithContactDto> e = locIterator.next();
                LOGGER.infof("key %s, value %s", e.key,e.value.toString());

            }


            //change some data
            contacts.pipeInput("c1", GenericContact.newBuilder().setContactId("c1").setName("WiTCOM GmbH 2").setFirstName("first name").build());

            enriched = store.all();
            while (enriched.hasNext()){
                KeyValue<String, LocationContactEnrichedDto> e = enriched.next();
                LOGGER.infof("key %s, value %s", e.key,e.value.toString());
            }





        }


    }

    private  <T extends SpecificRecord> SpecificAvroSerde<T> buildAvroValueSerde(Class<T> clazz) {
        HashMap<String, Object> serdeConfig = new HashMap<>();
        serdeConfig.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        SpecificAvroSerde<T> serde = new SpecificAvroSerde<>();
        serde.configure(serdeConfig, false);
        return serde;
    }

    
}
