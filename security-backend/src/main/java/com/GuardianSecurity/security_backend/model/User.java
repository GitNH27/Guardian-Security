package com.GuardianSecurity.security_backend.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import lombok.Data;          // Generates Getters, Setters, toString, equals, hashCode
import lombok.NoArgsConstructor; // Generates no-arg constructor (required by JPA)
import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor // Adds the JPA required empty constructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    // Custom Constructor (Retaing custom logic for initialization)
    public User(String email, String passwordHash, String firstName, String lastName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<DeviceAccess> deviceAccesses;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserFcmToken> fcmTokens = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    // Lombok will generate getPasswordHash() and setPasswordHash()
    private String passwordHash; 

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- JPA Lifecycle Callbacks (Preserved Logic) ---

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // --- START OF UserDetails INTERFACE IMPLEMENTATION (Preserved Logic) ---

    // REQUIRED by UserDetails: Returns the hashed password
    @Override
    public String getPassword() {
        // We MUST use the field name here, as Lombok generated a getter for passwordHash.
        return passwordHash; 
    }

    // REQUIRED by UserDetails: Returns the unique identifier used for login
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Current design uses device-specific roles, so return empty list for global authority.
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // --- END OF UserDetails INTERFACE IMPLEMENTATION ---
}