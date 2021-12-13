package de.witcom.tests.serde;

import org.apache.kafka.common.serialization.Serdes;

import de.witcom.tests.model.LocationAndContactDto;
import de.witcom.tests.model.LocationWithContactDto;
import de.witcom.tests.serde.json.JsonSerializer;
import de.witcom.tests.serde.json.JsonDeserializer;

public class LocationWithContactSerde extends Serdes.WrapperSerde<LocationWithContactDto>{

    public LocationWithContactSerde(){
        super(new JsonSerializer<>(),new JsonDeserializer<>(LocationWithContactDto.class));
    }
    
}
