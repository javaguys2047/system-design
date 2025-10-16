package com.systemdesign.urlshortner.service.dao;

import com.systemdesign.urlshortner.service.entity.UrlData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for URL data operations.
 * 
 * This DAO provides data access methods for UrlData entities including
 * finding by short ID, checking existence, and cleanup operations.
 */
@Repository
public interface UrlDataRepository extends JpaRepository<UrlData, Long> {

    /**
     * Finds a URL data entity by its short ID.
     * 
     * @param shortId The short ID to search for
     * @return Optional containing the URL data entity if found
     */
    Optional<UrlData> findByShortId(String shortId);

    /**
     * Checks if a short ID already exists in the database.
     * 
     * @param shortId The short ID to check
     * @return true if the short ID exists, false otherwise
     */
    boolean existsByShortId(String shortId);

    /**
     * Finds a URL data entity by its long URL.
     * 
     * @param longUrl The long URL to search for
     * @return Optional containing the URL data entity if found
     */
    Optional<UrlData> findByLongUrl(String longUrl);

    /**
     * Finds all non-expired URL data entries.
     * 
     * @param currentTime The current timestamp
     * @return List of non-expired URL data entities
     */
    @Query("SELECT u FROM UrlData u WHERE u.ttl > :currentTime")
    List<UrlData> findAllNonExpired(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Finds all expired URL data entries.
     * 
     * @param currentTime The current timestamp
     * @return List of expired URL data entities
     */
    @Query("SELECT u FROM UrlData u WHERE u.ttl <= :currentTime")
    List<UrlData> findAllExpired(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Deletes all expired URL data entries.
     * 
     * @param currentTime The current timestamp
     * @return Number of deleted entries
     */
    @Query("DELETE FROM UrlData u WHERE u.ttl <= :currentTime")
    int deleteExpiredEntries(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Finds a non-expired URL data entity by its short ID.
     * 
     * @param shortId The short ID to search for
     * @param currentTime The current timestamp
     * @return Optional containing the non-expired URL data entity if found
     */
    @Query("SELECT u FROM UrlData u WHERE u.shortId = :shortId AND u.ttl > :currentTime")
    Optional<UrlData> findNonExpiredByShortId(@Param("shortId") String shortId, 
                                             @Param("currentTime") LocalDateTime currentTime);
}
