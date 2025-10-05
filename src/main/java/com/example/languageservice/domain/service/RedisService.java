package com.example.languageservice.domain.service;

import com.example.languageservice.api.dto.ParagraphResponse;
import com.example.languageservice.domain.model.UserWord;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
//Maybe separate unseen key can be also added to avoid checking unseen in db? it can be updated immediately when user is assigned a word?
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    /* =========================
       Keys
       ========================= */
    private String kDueWords(UUID userId)                 { return "dueWords:" + userId; }
    private String kWordIndex(String word)                { return "word:" + word; }
    private String kParagraph(Long id)                    { return "para:" + id; }
    private String kLevelWordIds(String level, String language) {
        return "level:" + level + ":" + language + ":wordIds";
    }
    private String kUserAssigned(UUID userId, String level, String language) {
        return "user:" + userId + ":" + level + ":" + language + ":assigned";
    }

    /* =========================
       Paragraph storage (already exists)
       ========================= */

    // Save word -> paragraph reverse index
    public void saveParagraphIndex(Long paragraphId, Iterable<String> words) {
        for (String word : words) {
            redisTemplate.opsForSet().add(kWordIndex(word), kParagraph(paragraphId));
        }
    }

    // Save full paragraph
    public void saveParagraphContent(Long paragraphId, ParagraphResponse response) {
        try {
            redisTemplate.opsForValue().set(kParagraph(paragraphId), mapper.writeValueAsString(response));
        } catch (Exception e) {
            throw new RuntimeException("Failed to save paragraph in Redis", e);
        }
    }

    // Lookup by words (superset)
    public Set<String> intersectWords(Set<String> words) {
        if (words.isEmpty()) return Set.of();
        String[] keys = words.stream().map(this::kWordIndex).toArray(String[]::new);
        return redisTemplate.opsForSet().intersect(keys[0], List.of(keys));
    }

    // Retrieve full paragraph by ID
    public ParagraphResponse getParagraph(Long paragraphId) {
        try {
            String json = redisTemplate.opsForValue().get(kParagraph(paragraphId));
            if (json == null) return null;
            return mapper.readValue(json, ParagraphResponse.class);
        } catch (Exception e) {
            return null;
        }
    }

    /* =========================
       Word & Box — due words cache
       ========================= */

    // Save user due words (serialize whole list)
    public void cacheDueWords(UUID userId, List<UserWord> dueWords) {
        try {
            String json = mapper.writeValueAsString(dueWords);
            redisTemplate.opsForValue().set(kDueWords(userId), json, Duration.ofHours(12));
        } catch (Exception e) {
            throw new RuntimeException("Failed to cache due words for user " + userId, e);
        }
    }

    // Get cached user due words
    public List<UserWord> getCachedDueWords(UUID userId) {
        try {
            String json = redisTemplate.opsForValue().get(kDueWords(userId));
            if (json == null) return null;
            return Arrays.asList(mapper.readValue(json, UserWord[].class));
        } catch (Exception e) {
            return null;
        }
    }

    // Invalidate due words
    public void invalidateDueWords(UUID userId) {
        redisTemplate.delete(kDueWords(userId));
    }

    /* =========================
       NEW: Random word supply per user/level/language
       ========================= */

    /**
     * Returns ALL unseen word IDs in Redis for this user+level+language:
     * unseen = SDIFF(level:<level>:<language>:wordIds, user:<uuid>:<level>:<language>:assigned)
     * You can limit/trim in the service layer.
     */
    public List<Long> getUnseenWordsForUser(UUID userId, String level, String language) {
        String levelKey = kLevelWordIds(level, language);
        String userAssignedKey = kUserAssigned(userId, level, language);

        // Server-side SDIFF if both sets exist, else graceful fallback
        Set<String> unseen = redisTemplate.opsForSet().difference(levelKey, userAssignedKey);
        if (unseen == null || unseen.isEmpty()) return List.of();

        return unseen.stream().map(Long::valueOf).toList();
    }

    /**
     * Convenience to fetch up to 'count' unseen random IDs (uses SRANDMEMBER on the DIFF result in-memory).
     * If you prefer pure Redis-side, you could store SDIFFSTORE into a temp key, then SRANDMEMBER from that key.
     */
    public List<Long> getRandomUnseenWordsForUser(UUID userId, String level, String language, int count) {
        List<Long> unseen = getUnseenWordsForUser(userId, level, language);
        if (unseen.isEmpty()) return List.of();
        if (unseen.size() <= count) return unseen;

        // random sample locally
        Collections.shuffle(unseen);
        return unseen.subList(0, count);
    }

    /**
     * Prime the level's global pool (idempotent). You can call this at boot or when you ingest new words.
     * This set holds ALL word IDs for that CEFR level and language, independent of user.
     */
    public void addLevelWordIds(String level, String language, Collection<Long> wordIds) {
        if (wordIds == null || wordIds.isEmpty()) return;
        String levelKey = kLevelWordIds(level, language);
        String[] arr = wordIds.stream().map(String::valueOf).toArray(String[]::new);
        redisTemplate.opsForSet().add(levelKey, arr);
    }

    /**
     * Mark words as assigned to a user in Redis (so future calls exclude them).
     * Call this whenever you assign words to the user in DB.
     */
    public void addUserAssignedWords(UUID userId, String level, String language, Collection<Long> wordIds) {
        if (wordIds == null || wordIds.isEmpty()) return;
        String userKey = kUserAssigned(userId, level, language);
        String[] arr = wordIds.stream().map(String::valueOf).toArray(String[]::new);
        redisTemplate.opsForSet().add(userKey, arr);
        // generally no TTL for this set; it's a long-lived mirror of DB assignment
    }

    /**
     * Optional: remove assignment (e.g., user unassigns a word)
     */
    public void removeUserAssignedWords(UUID userId, String level, String language, Collection<Long> wordIds) {
        if (wordIds == null || wordIds.isEmpty()) return;
        String userKey = kUserAssigned(userId, level, language);
        String[] arr = wordIds.stream().map(String::valueOf).toArray(String[]::new);
        redisTemplate.opsForSet().remove(userKey, (Object[]) arr);
    }

    /**
     * Optional: bulk load user assigned set (e.g., first time cache warm-up from DB).
     * You can call this from a service if you detect the user set is missing.
     */
    public void replaceUserAssignedFromDb(UUID userId, String level, String language, Collection<Long> dbAssignedIds) {
        String userKey = kUserAssigned(userId, level, language);
        redisTemplate.delete(userKey);
        addUserAssignedWords(userId, level, language, dbAssignedIds);
    }

    /* =========================
       Utils for generic list caching (optional reuse)
       ========================= */

    public <T> void saveList(String key, List<T> list, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, mapper.writeValueAsString(list), ttl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save list to Redis for key " + key, e);
        }
    }

    public <T> List<T> getList(String key, Class<T> clazz) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) return null;
            T[] arr = mapper.readValue(json, mapper.getTypeFactory().constructArrayType(clazz));
            return Arrays.asList(arr);
        } catch (Exception e) {
            return null;
        }
    }

    public void invalidate(String key) {
        redisTemplate.delete(key);
    }
}
