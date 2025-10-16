package com.systemdesign.urlshortner.service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing URL data with short ID, long URL, and TTL (Time To Live).
 * 
 * This entity stores the mapping between short codes and original URLs,
 * along with expiration time for automatic cleanup.
 */
@Entity
@Table(name = "url_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_id", nullable = false, unique = true, length = 10)
    private String shortId;

    @Column(name = "long_url", nullable = false, columnDefinition = "TEXT")
    private String longUrl;

    @Column(name = "ttl", nullable = false)
    private LocalDateTime ttl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating a new URL data entry.
     * 
     * @param shortId The generated short identifier
     * @param longUrl The original long URL
     * @param ttl The time to live (expiration time)
     */
    public UrlData(String shortId, String longUrl, LocalDateTime ttl) {
        this.shortId = shortId;
        this.longUrl = longUrl;
        this.ttl = ttl;
    }

    /**
     * Checks if the URL data has expired.
     * 
     * @return true if the current time is past the TTL, false otherwise
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.ttl);
    }
}
