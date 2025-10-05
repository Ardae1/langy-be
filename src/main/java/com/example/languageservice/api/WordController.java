package com.example.languageservice.api;

import com.example.languageservice.api.dto.UserRandomWordRequest;
import com.example.languageservice.domain.model.Word;
import com.example.languageservice.domain.service.BoxService;
import com.example.languageservice.domain.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/words")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;
    private final BoxService boxService;

    @GetMapping("/generate")
    public ResponseEntity<Void> addWord(
            @AuthenticationPrincipal User user,
            @RequestBody String wordText) {

        Word word = wordService.saveIfNotExists(wordText);
        boxService.assignToInit(user, word);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/generate-random")
    public ResponseEntity<String> generateRandomWord( UserRandomWordRequest request) {
        int count = request.getCount();
        String language = request.getSourceLanguage();
        String level = request.getLevel();
        String randomWord = wordService.getRandomWords(count, language, level);
        return ResponseEntity.ok(randomWord);
    }
}
