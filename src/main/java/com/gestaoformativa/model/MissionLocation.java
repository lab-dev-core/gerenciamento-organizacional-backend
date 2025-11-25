package com.gestaoformativa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "mission_locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    private String country;

    private String address;

    private String postalCode;

    @ManyToOne
    @JoinColumn(name = "coordinator_id")
    private User coordinator;

    @OneToMany(mappedBy = "missionLocation")
    private List<User> assignedUsers;

    public String getFullAddress() {
        StringBuilder addressBuilder = new StringBuilder();

        if (address != null && !address.isEmpty()) {
            addressBuilder.append(address).append(", ");
        }

        addressBuilder.append(city).append(" - ").append(state);

        if (country != null && !country.isEmpty()) {
            addressBuilder.append(", ").append(country);
        }

        if (postalCode != null && !postalCode.isEmpty()) {
            addressBuilder.append(" (").append(postalCode).append(")");
        }

        return addressBuilder.toString();
    }

    public int getUserCount() {
        return assignedUsers != null ? assignedUsers.size() : 0;
    }
}
