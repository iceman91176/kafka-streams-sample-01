package de.witcom.tests.serde;

import javax.enterprise.context.ApplicationScoped;

public class SerdesFactory {

    public static LocationContactEnrichedSerde locationContactEnrichedSerde(){

        return new LocationContactEnrichedSerde();

    }

    public static LocationAndContactSerde locationAndContactSerde(){
        return new LocationAndContactSerde();
    }

    public static LocationWithContactSerde locationWithContactSerde(){
        return new LocationWithContactSerde();
    }
    
}
