package com.aeroklub.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity @Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User {
    public enum Role { ADMIN, USER, PILOT }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Role role = Role.USER;

    /** jti of the single valid refresh token (rotation / reuse detection). */
    private String refreshTokenId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Flight> flights = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "user_license",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "license_id"))
    private Set<License> licenses = new HashSet<>();
}