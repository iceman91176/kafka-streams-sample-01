package de.witcom.tests.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationContactEnrichedDto {

    String id;
    String locId;
    String contactId;
    String name;
    String firstName;

}
