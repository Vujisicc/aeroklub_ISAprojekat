package com.aeroklub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity @Table(name = "gliders")
@Getter @Setter @NoArgsConstructor
public class Glider {
    public enum Status { ACTIVE, MAINTENANCE }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String registrationMarks;

    @Column(nullable = false)
    private String model;

    @Column(name = "production_year", nullable = false)
    private int year;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @JsonIgnore
    @OneToMany(mappedBy = "glider")
    private List<Flight> flights = new ArrayList<>();
}