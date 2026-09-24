package com.aeroklub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity @Table(name = "licenses")
@Getter @Setter @NoArgsConstructor
public class License {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String countryOfIssue;

    @JsonIgnore
    @ManyToMany(mappedBy = "licenses")
    private Set<User> users = new HashSet<>();
}