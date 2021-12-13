package de.witcom.tests.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.witcom.test.schema.avro.location.GenericContact;
import lombok.Data;

@Data
public class LocationWithContactDto {

    public String locId;
    public String name;
    public List<LocationContactEnrichedDto> contacts =  new ArrayList<>();

    public LocationWithContactDto addContact(LocationAndContactDto locationAndContact){

        contacts.add(locationAndContact.getLocationContact());
        return this;
    }

    public LocationWithContactDto removeContact(LocationAndContactDto locationAndContact){

        Iterator<LocationContactEnrichedDto> it = contacts.iterator();
        while (it.hasNext()) {
            LocationContactEnrichedDto c = it.next();
            if (c.id == locationAndContact.getLocationContact().getId()) {
                it.remove();
                break;
            }
        }

        return this;
    }



    

    
}
