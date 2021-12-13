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
import org.apache.kafka.common.utils.Bytes;
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
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import de.witcom.test.schema.avro.location.GenericContact;
import de.witcom.test.schema.avro.location.GenericLocation;
import de.witcom.test.schema.avro.location.GenericLocationContact;
import de.witcom.test.schema.avro.location.GenericLocationContactEnriched;
import de.witcom.tests.model.LocationAndContactDto;
import de.witcom.tests.model.LocationContactEnrichedDto;
import de.witcom.tests.model.LocationDto;
import de.witcom.tests.model.LocationWithContactDto;
import de.witcom.tests.serde.LocationAndContactSerde;
import de.witcom.tests.serde.LocationContactEnrichedSerde;
import de.witcom.tests.serde.SerdesFactory;
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

    @ConfigProperty(name = "location.topic")
    String locationTopic;


    @ConfigProperty(name = "kafka.schema.registry.url")
    String schemaRegistryUrl;


    @Produces
    public Topology buildTopology() {

        StreamsBuilder builder = new StreamsBuilder();

        Serde<GenericLocationContact> locationContactSerde= getSpecificAvroSerde(schemaRegistryUrl);
        Serde<GenericContact> contactSerde= getSpecificAvroSerde(schemaRegistryUrl);
        Serde<GenericLocation> locationSerde = getSpecificAvroSerde(schemaRegistryUrl);


        KTable<String, GenericLocationContact> locationContacts = builder.table(locationContactTopic, Consumed.with(Serdes.String(), locationContactSerde));
        KTable<String, GenericContact> contacts = builder.table(contactTopic, Consumed.with(Serdes.String(), contactSerde));
        KTable<String, GenericLocation> locations = builder.table(locationTopic, Consumed.with(Serdes.String(), locationSerde));

        //intermediate stuff
        LocationContactEnrichedSerde enrichedLocContactSerde = new LocationContactEnrichedSerde();
        LocationAndContactSerde locationAndContactSerde = SerdesFactory.locationAndContactSerde();


        //Join Location Contacts with Contacts        
        KTable<String, LocationContactEnrichedDto> res = locationContacts.join(contacts,
            //fk extractor
            this::extractKey,
            //build new dto
            new LocationContactEnrichedJoiner(),
            //this::toGenericLocationContactEnriched,
            // store into materialized view with name LOC-CONTACT-ENRICHED-MV
            Materialized.<String, LocationContactEnrichedDto, KeyValueStore<Bytes, byte[]>>
            as("LOC-CONTACT-ENRICHED-MV")
                .withKeySerde(Serdes.String())
                .withValueSerde(enrichedLocContactSerde)
        );

        
        KTable<String, LocationWithContactDto> contactEnrichedLocations = res.join(locations,
         this::extractLocationKey,
         this::toLocationAndContactDto,
         Materialized.with(Serdes.String(), locationAndContactSerde)
        )
        .groupBy(
            (contactId,locationAndContact) -> KeyValue.pair(locationAndContact.getLocationContact().getContactId(),locationAndContact),
            Grouped.with(Serdes.String(), locationAndContactSerde)
        )
        .aggregate(
            LocationWithContactDto::new,
            (customerId, addressAndCustomer, aggregate) -> aggregate.addContact(addressAndCustomer),
            (customerId, addressAndCustomer, aggregate) -> aggregate.removeContact(addressAndCustomer),
            Materialized.<String, LocationWithContactDto, KeyValueStore<Bytes, byte[]>>
            as("LOC-ENRICHED-MV")
                .withKeySerde(Serdes.String())
                .withValueSerde(SerdesFactory.locationWithContactSerde())
        )
        ;

        //other = locations
        //extractor = extractLocationKey (location id aus LocationContactEnrichedDto )
        //joiner = toLocationAndContactDto
        //KTable<String, LocationAndContactDto> res2 = res.join(locations, this::extractLocationKey, this::toLocationAndContactDto, Materialized.with(Serdes.Long(), locationAndContactSerde)); 

        /*
        //join location and enriched loccationcontacts
        locations.join(res,
        this::extractLocationKey,
        this::toLocationAndContactDto,
        Materialized.with(Serdes.String(), locationAndContactSerde)
        );
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
    /*
    private String extractLocationKey(GenericLocation location){
        LOGGER.infof("Extracting location-id %s", location.getLocId());
        return location.getLocId().toString();
    } */    

    private String extractKey(GenericLocationContact locContact){
        LOGGER.infof("Extract contact-id %s", locContact.getContactId());
        return locContact.getContactId().toString();
    }     

    private String extractLocationKey(LocationContactEnrichedDto locContact){
        LOGGER.infof("Extract loc-id %s", locContact.getLocId());
        return locContact.getLocId();
    } 

    private String extractLocationKeySimple(GenericLocationContact locContact){
        LOGGER.infof("Extract loc-id %s", locContact.getLocId());
        return locContact.getLocId().toString();
    }


   

    private GenericLocation simpleJoiner(GenericLocationContact locContact,GenericLocation location){

        location.setName("bla");
        return location;
        //LocationDto locationDto = LocationDto.builder().locId(location.getLocId().toString()).name(location.getName().toString()).build();
        //return LocationAndContactDto.builder().locationContact(locContact).location(locationDto).build();
    }
    
    private LocationAndContactDto toLocationAndContactDto(LocationContactEnrichedDto locContact,GenericLocation location){

        LocationDto locationDto = LocationDto.builder().locId(location.getLocId().toString()).name(location.getName().toString()).build();
        return LocationAndContactDto.builder().locationContact(locContact).location(locationDto).build();
        
    } 
    
    private GenericLocationContactEnriched toGenericLocationContactEnriched(GenericLocationContact locContact,GenericContact contact){

        LOGGER.infof("Location Id in loc-contact: %s", locContact.getLocId());
        LOGGER.infof("Contact-Name in contact: %s", contact.getName().toString());
        return GenericLocationContactEnriched.newBuilder()
        .setId(locContact.getId())
        .setLocId(locContact.getLocId())
        .setName(contact.getName())
        .setContactId(contact.getContactId())
        .setFirstName(contact.getFirstName())
        .build();
    }


   

}
