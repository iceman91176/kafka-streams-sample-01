package de.witcom.tests.streams;

import org.apache.kafka.streams.kstream.ValueJoiner;

import de.witcom.test.schema.avro.location.GenericContact;
import de.witcom.test.schema.avro.location.GenericLocationContact;
import de.witcom.test.schema.avro.location.GenericLocationContactEnriched;
import de.witcom.tests.model.LocationContactEnrichedDto;

public class LocationContactEnrichedJoiner implements ValueJoiner<GenericLocationContact,GenericContact,LocationContactEnrichedDto> {

    @Override
    public LocationContactEnrichedDto apply(GenericLocationContact locContact, GenericContact contact) {


        return LocationContactEnrichedDto.builder()
            .id(locContact.getId().toString())
            .locId(locContact.getLocId().toString())
            .contactId(contact.getContactId().toString())
            .name(contact.getName().toString())
            //.firstName(contact.getFirstName().toString())
            .build();
        
        /*    
        return GenericLocationContactEnriched.newBuilder()
            .setId(locContact.getId())
            .setLocId(locContact.getLocId())
            .setName(contact.getName())
            .setContactId(contact.getContactId())
            .setFirstName(contact.getFirstName())
            .build();
        */    
    }
    
}
