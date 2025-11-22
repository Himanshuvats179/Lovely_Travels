package com.example.demo.entity.Hotel;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "hotels")
public class Hotels {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Column(nullable = false)
    private String name;

    // Maps the JSONB column. Often requires a custom Hibernate type (JsonType) or String mapping.
    @Column(name = "address_json", columnDefinition = "jsonb")
    // @Type(JsonType.class)
    private String addressJson; // Storing as String for simple mapping

    // For GEOGRAPHY type (PostGIS), similar to Cab service, use a String placeholder
    @Column(name = "coordinates")
    private String coordinatesWkt;
}