package com.example.languageservice.domain.service;

import com.example.languageservice.api.dto.ParagraphResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Save paragraph to reverse index
    public void saveParagraphIndex(Long paragraphId, Iterable<String> words) {
        for (String word : words) {
            redisTemplate.opsForSet().add("word:" + word, "para:" + paragraphId);
        }
    }

    // Save full paragraph object
    public void saveParagraphContent(Long paragraphId, ParagraphResponse response) {
        try {
            redisTemplate.opsForValue().set("para:" + paragraphId, mapper.writeValueAsString(response));
        } catch (Exception e) {
            throw new RuntimeException("Failed to save paragraph in Redis", e);
        }
    }

    // Lookup by words (superset)
    public Set<String> intersectWords(Set<String> words) {
        if (words.isEmpty()) return null;
        String[] keys = words.stream().map(w -> "word:" + w).toArray(String[]::new);
        return redisTemplate.opsForSet().intersect(keys[0], List.of(keys));
    }

    // Retrieve full paragraph by ID
    public ParagraphResponse getParagraph(Long paragraphId) {
        try {
            String json = redisTemplate.opsForValue().get("para:" + paragraphId);
            if (json == null) return null;
            return mapper.readValue(json, ParagraphResponse.class);
        } catch (Exception e) {
            return null;
        }
    }
}
