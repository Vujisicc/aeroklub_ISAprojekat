package com.aeroklub.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name = "flights")
@Getter @Setter @NoArgsConstructor
public class Flight {
    public enum LaunchType { WINCH, TOW }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_date", nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private int durationMinutes;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private LaunchType launchType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "glider_id")
    private Glider glider;
}