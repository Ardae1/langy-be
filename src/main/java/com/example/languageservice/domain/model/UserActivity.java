package com.example.languageservice.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "user_activities")
public class UserActivity {

    // Getters and setters
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID sessionId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String actionType;

    @Column
    private String actionDetails;

    @Column(nullable = false)
    private Instant timestamp;

}