package de.witcom.tests.serde;

import org.apache.kafka.common.serialization.Serdes;


import de.witcom.tests.model.LocationContactEnrichedDto;
import de.witcom.tests.serde.json.JsonSerializer;
import de.witcom.tests.serde.json.JsonDeserializer;

public class LocationContactEnrichedSerde extends Serdes.WrapperSerde<LocationContactEnrichedDto>{

    public LocationContactEnrichedSerde(){
        super(new JsonSerializer<>(),new JsonDeserializer<>(LocationContactEnrichedDto.class));
    }
    
}
