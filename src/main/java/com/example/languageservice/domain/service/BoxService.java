package com.example.languageservice.domain.service;

import com.example.languageservice.domain.model.Box;
import com.example.languageservice.domain.model.BoxType;
import com.example.languageservice.domain.model.UserWord;
import com.example.languageservice.domain.model.Word;
import com.example.languageservice.domain.repository.BoxRepository;
import com.example.languageservice.domain.repository.UserWordRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoxService {

    private final BoxRepository boxRepository;
    private final WordService wordService;
    private final RedisService redisService;

    // Promote to next box
    @Transactional
    public void promoteWordToNewBox(UserWord userWord) {
        BoxType nextType = BoxType.nextBoxMap.get(userWord.getBox().getId());
        Box nextBox = boxRepository.findByType(nextType).orElseThrow();
        wordService.updateUserWordBox(userWord, nextBox);

        redisService.invalidateDueWords(userWord.getUser().getId());
    }

    // Reset to UNKNOWN_1DAY
    @Transactional
    public void resetToUnknown(UserWord userWord) {
        Box resetBox = boxRepository.findByType(BoxType.UNKNOWN_1DAY).orElseThrow();
        wordService.updateUserWordBox(userWord, resetBox);

        redisService.invalidateDueWords(userWord.getUser().getId());
    }

    // Assign brand new word to INIT box
    @Transactional
    public void assignToInit(User user, Word word) {
        Box initBox = boxRepository.findByType(BoxType.INIT).orElseThrow();
        wordService.createUserWord(user, word, initBox);

        redisService.invalidateDueWords(user.getId());
    }

    @Transactional(readOnly = true)
    public Box getBox(BoxType type) {
        return boxRepository.findByType(type).orElseThrow();
    }

    // Fetch due words with Redis cache
    public List<UserWord> getDueWords(Long userId) {
        List<UserWord> cached = redisService.getCachedDueWords(userId);
        if (cached != null) return cached;

        List<UserWord> dueWords = wordService.findDueWords(userId, LocalDateTime.now());
        if (!dueWords.isEmpty()) {
            redisService.cacheDueWords(userId, dueWords);
        }
        return dueWords;
    }

    @PostConstruct
    public void initBoxes() {
        for (BoxType type : BoxType.values()) {
            if (boxRepository.findById(type.getId()).isEmpty()) {
                boxRepository.save(new Box(type.getId(), type.getName(), type.getIntervalDays()));
            }
        }
    }
}
