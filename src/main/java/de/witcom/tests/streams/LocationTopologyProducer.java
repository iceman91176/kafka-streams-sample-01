package de.witcom.tests.streams;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import de.witcom.test.schema.avro.location.GenericContact;
import de.witcom.test.schema.avro.location.GenericLocationContact;
import de.witcom.test.schema.avro.location.GenericLocationContactEnriched;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.jboss.logging.Logger;

@ApplicationScoped
public class LocationTopologyProducer {

    private static final Logger LOGGER =
    Logger.getLogger("LocationTopologyProducer");

    final static String SCHEMA_REGISTRY_URL_CONFIG="schema.registry.url";

    @ConfigProperty(name = "contact.topic")
    String contactTopic;

    @ConfigProperty(name = "location-contact.topic")
    String locationContactTopic;

    @ConfigProperty(name = "kafka.schema.registry.url")
    String schemaRegistryUrl;


    @Produces
    public Topology buildTopology() {

        StreamsBuilder builder = new StreamsBuilder();

        Serde<GenericLocationContact> locationContactSerde= getSpecificAvroSerde(schemaRegistryUrl);
        Serde<GenericContact> contactSerde= getSpecificAvroSerde(schemaRegistryUrl);
        Serde<GenericLocationContactEnriched> imSerde = getSpecificAvroSerde(schemaRegistryUrl);

        KTable<String, GenericLocationContact> locationContacts = builder.table(locationContactTopic, Consumed.with(Serdes.String(), locationContactSerde));
        KTable<String, GenericContact> contacts = builder.table(contactTopic, Consumed.with(Serdes.String(), contactSerde));

        locationContacts.join(contacts,this::extractKey,this::toGenericLocationContactEnriched).toStream().peek((k,v) -> {
            //LOGGER.infof("Joined loccontact and Contact to %s %s", k,v.toString());
            LOGGER.infof("Joined loccontact and Contact on key %s ", k);
            if(v!=null){
                LOGGER.infof("Joined loccontact and Contact to %s %s", k,v.toString());
            }
        }).to("intermediate-topic", Produced.with(Serdes.String(),imSerde));

        /*
        locationContacts.join(contacts, this::toGenericLocationContactEnriched).toStream().peek((k,v) -> {
            LOGGER.infof("Joined loccontact and Contact to %s %s", k,v.toString());
        }).to("intermediate-topic", Produced.with(Serdes.String(),imSerde));
        */

        return builder.build();

    }


    //SuppressWarnings("unchecked")
    static <T> Serde<T> getPrimitiveAvroSerde(final String registryUrl, boolean isKey) {
        final KafkaAvroDeserializer deserializer = new KafkaAvroDeserializer();
        final KafkaAvroSerializer serializer = new KafkaAvroSerializer();
        final Map<String, String> config = new HashMap<>();
        config.put(SCHEMA_REGISTRY_URL_CONFIG,registryUrl );
        deserializer.configure(config, isKey);
        serializer.configure(config, isKey);
        return (Serde<T>)Serdes.serdeFrom(serializer, deserializer);
    }

    static <T extends SpecificRecord> SpecificAvroSerde<T> getSpecificAvroSerde(final String registryUrl) {
        final SpecificAvroSerde<T> specificAvroSerde = new SpecificAvroSerde<>();

        final HashMap<String, String> serdeConfig = new HashMap<>();
        serdeConfig.put(SCHEMA_REGISTRY_URL_CONFIG,registryUrl);

        specificAvroSerde.configure(serdeConfig, false);
        return specificAvroSerde;
    }
    
    private String extractKey(GenericLocationContact locContact){
        LOGGER.infof("Extract contact-id %s", locContact.getContactId());
        return locContact.getContactId().toString();
    } 
    
    private GenericLocationContactEnriched toGenericLocationContactEnriched(GenericLocationContact locContact,GenericContact contact){

        LOGGER.infof("Location Id in loc-contact", locContact.getLocId());
        LOGGER.infof("Contact-Name in contact", contact.getName().toString());
        return GenericLocationContactEnriched.newBuilder()
        .setId(locContact.getId())
        .setLocId(locContact.getLocId())
        .setName(contact.getName())
        .setContactId(contact.getContactId())
        .setFirstName(contact.getFirstName())
        .build();
    }
    

}
