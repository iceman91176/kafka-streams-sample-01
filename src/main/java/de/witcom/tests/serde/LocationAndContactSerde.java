package de.witcom.tests.serde;

import org.apache.kafka.common.serialization.Serdes;

import de.witcom.tests.model.LocationAndContactDto;

import de.witcom.tests.serde.json.JsonSerializer;
import de.witcom.tests.serde.json.JsonDeserializer;

public class LocationAndContactSerde extends Serdes.WrapperSerde<LocationAndContactDto>{

    public LocationAndContactSerde(){
        super(new JsonSerializer<>(),new JsonDeserializer<>(LocationAndContactDto.class));
    }
    
}
