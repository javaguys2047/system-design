package com.systemdesign.urlshortner.service;

import com.systemdesign.urlshortner.service.dao.UrlDataRepository;
import com.systemdesign.urlshortner.service.entity.UrlData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service component for URL shortening operations.
 * 
 * Provides business logic for creating short URLs, retrieving original URLs,
 * managing TTL (Time To Live), and cleanup operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {

    private final UrlDataRepository urlDataRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    
    // Base62 characters for generating short IDs
    private static final String BASE62_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SHORT_ID_LENGTH = 6;
    
    // Default TTL: 30 days from creation
    private static final int DEFAULT_TTL_DAYS = 30;

    /**
     * Creates a short URL for the given original URL with default TTL.
     * 
     * @param longUrl The original URL to shorten
     * @return The shortened URL ID
     * @throws IllegalArgumentException if the URL is invalid
     */
    @Transactional
    public String createShortUrl(String longUrl) {
        return createShortUrl(longUrl, DEFAULT_TTL_DAYS);
    }

    /**
     * Creates a short URL for the given original URL with custom TTL.
     * 
     * @param longUrl The original URL to shorten
     * @param ttlDays Number of days until expiration
     * @return The shortened URL ID
     * @throws IllegalArgumentException if the URL is invalid
     */
    @Transactional
    public String createShortUrl(String longUrl, int ttlDays) {
        validateUrl(longUrl);
        
        // Check if URL already exists and is not expired
        Optional<UrlData> existingUrl = urlDataRepository.findByLongUrl(longUrl);
        if (existingUrl.isPresent() && !existingUrl.get().isExpired()) {
            log.info("URL already exists with short ID: {}", existingUrl.get().getShortId());
            return existingUrl.get().getShortId();
        }

        // Generate unique short ID
        String shortId = generateUniqueShortId();
        
        // Calculate TTL
        LocalDateTime ttl = LocalDateTime.now().plusDays(ttlDays);
        
        // Create and save URL data entity
        UrlData urlData = new UrlData(shortId, longUrl, ttl);
        urlDataRepository.save(urlData);
        
        log.info("Created short URL: {} -> {} (TTL: {})", shortId, longUrl, ttl);
        return shortId;
    }

    /**
     * Retrieves the original URL for a given short ID if not expired.
     * 
     * @param shortId The short ID to resolve
     * @return Optional containing the original URL if found and not expired
     */
    @Transactional
    public Optional<String> getOriginalUrl(String shortId) {
        Optional<UrlData> urlData = urlDataRepository.findByShortId(shortId);
        
        if (urlData.isPresent()) {
            UrlData data = urlData.get();
            if (!data.isExpired()) {
                log.info("Retrieved URL: {} -> {}", shortId, data.getLongUrl());
                return Optional.of(data.getLongUrl());
            } else {
                log.warn("URL has expired: {}", shortId);
                // Optionally delete expired entry
                urlDataRepository.delete(data);
                return Optional.empty();
            }
        }
        
        log.warn("Short ID not found: {}", shortId);
        return Optional.empty();
    }

    /**
     * Gets URL data for a given short ID.
     * 
     * @param shortId The short ID to get data for
     * @return Optional containing the URL data entity
     */
    public Optional<UrlData> getUrlData(String shortId) {
        return urlDataRepository.findByShortId(shortId);
    }

    /**
     * Gets all non-expired URL data entries.
     * 
     * @return List of non-expired URL data entities
     */
    public List<UrlData> getAllNonExpiredUrls() {
        return urlDataRepository.findAllNonExpired(LocalDateTime.now());
    }

    /**
     * Cleans up expired URL entries from the database.
     * 
     * @return Number of deleted entries
     */
    @Transactional
    public int cleanupExpiredUrls() {
        int deletedCount = urlDataRepository.deleteExpiredEntries(LocalDateTime.now());
        log.info("Cleaned up {} expired URL entries", deletedCount);
        return deletedCount;
    }

    /**
     * Validates if the given string is a valid URL.
     * 
     * @param url The URL string to validate
     * @throws IllegalArgumentException if the URL is invalid
     */
    private void validateUrl(String url) {
        try {
            new URL(url);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL format: " + url);
        }
    }

    /**
     * Generates a unique short ID.
     * 
     * @return A unique short ID
     */
    private String generateUniqueShortId() {
        String shortId;
        do {
            shortId = generateRandomShortId();
        } while (urlDataRepository.existsByShortId(shortId));
        
        return shortId;
    }

    /**
     * Generates a random short ID using base62 characters.
     * 
     * @return A random short ID
     */
    private String generateRandomShortId() {
        StringBuilder shortId = new StringBuilder();
        for (int i = 0; i < SHORT_ID_LENGTH; i++) {
            shortId.append(BASE62_CHARS.charAt(secureRandom.nextInt(BASE62_CHARS.length())));
        }
        return shortId.toString();
    }
}
