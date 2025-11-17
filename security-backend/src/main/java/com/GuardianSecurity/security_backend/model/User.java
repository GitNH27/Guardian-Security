package com.GuardianSecurity.security_backend.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority; // Import for UserDetails
import org.springframework.security.core.userdetails.UserDetails; // Import for UserDetails
import java.time.LocalDateTime;
import java.util.Collection; // Import for UserDetails methods
import java.util.Collections; // Import for Collections.emptyList()

@Entity
@Table(name = "users")
public class User implements UserDetails { // <--- ADD THIS: implements UserDetails

    // No-argument constructor just for JPA
    public User() {
    }

    // Default constructor
    public User(String email, String passwordHash, String firstName, String lastName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and setters (these remain the same)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    // Renamed from getPasswordHash to getPassword to match UserDetails interface
    // You can keep getPasswordHash() if other parts of your code use it,
    // but getPassword() is required by UserDetails.
    // Ensure this method returns the actual hashed password.
    public String getPassword() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

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

    // --- START OF UserDetails INTERFACE IMPLEMENTATION ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // As discussed, your current schema doesn't have global roles for users.
        // So, we return an empty list of authorities.
        // Device-specific roles (OWNER/MEMBER) are checked in service layer logic.
        return Collections.emptyList();
    }

    @Override
    public String getUsername() {
        // THIS IS THE CRUCIAL PART!
        // Spring Security will call this to get the user's principal name.
        // You want this to be the email for your application.
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        // Implement logic if you have account expiration, otherwise return true.
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Implement logic if you have account locking, otherwise return true.
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Implement logic if you have password expiration, otherwise return true.
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Implement logic if you have account enablement/disablement, otherwise return true.
        return true;
    }

    // --- END OF UserDetails INTERFACE IMPLEMENTATION ---
}