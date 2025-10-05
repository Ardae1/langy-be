package com.example.languageservice.api;

import com.example.languageservice.domain.model.UserWord;
import com.example.languageservice.domain.repository.UserWordRepository;
import com.example.languageservice.domain.service.BoxService;
import com.example.languageservice.domain.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final BoxService boxService;
    private final WordService wordService;

    // 1. Get due words
    @GetMapping("/due")
    public ResponseEntity<List<UserWord>> getDueWords(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(boxService.getDueWords(user.getId()));
    }

    // 2. Submit answer
    //can be a request object instead of individual params
    @PostMapping("/{userId}/submit")
    public ResponseEntity<String> submitReview(
            @PathVariable UUID userId,
            @RequestParam Long wordId,
            @RequestParam boolean correct
    ) {
        UserWord userWord = wordService.findUserWord(userId, wordId)
                .orElseThrow(() -> new IllegalArgumentException("UserWord not found"));

        if (correct) {
            boxService.promoteWordToNewBox(userWord);
            return ResponseEntity.ok("Correct! Word promoted.");
        } else {
            boxService.resetToUnknown(userWord);
            return ResponseEntity.ok("Incorrect! Word sent back to UNKNOWN_1DAY.");
        }
    }
}
