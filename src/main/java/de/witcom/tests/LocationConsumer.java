package de.witcom.tests;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import de.witcom.test.schema.avro.location.GenericLocation;

import javax.enterprise.context.ApplicationScoped;

//@ApplicationScoped
public class LocationConsumer {

    private static final Logger LOGGER =
        Logger.getLogger("LocationConsumer");
    
    @Incoming("locations-from-kafka")
    public void receive(GenericLocation location){
        LOGGER.infof("Received location: %s %s", location.getLocId(),location.getName());
    }
}
