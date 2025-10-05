package com.example.languageservice.domain.service;

import com.example.languageservice.api.dto.UserRandomWordRequest;
import com.example.languageservice.api.dto.UserWordRequest;
import com.example.languageservice.api.dto.WordDto;
import com.example.languageservice.domain.model.*;
import com.example.languageservice.domain.repository.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.languageservice.security.SecurityUtils.getCurrentUserId;

@Service
@RequiredArgsConstructor
public class WordService {

    private final WordRepository wordRepository;
    private final WordFormRepository wordFormRepository;
    private final WordConjugationRepository wordConjugationRepository;
    private final WordExampleRepository wordExampleRepository;
    private final WordTipRepository wordTipRepository;
    private final WordRelationRepository wordRelationRepository;
    private final UserWordRepository userWordRepository;
    private final WordTranslationRepository wordTranslationRepository;
    private final RedisService redisService;
    private final LlmService llmService; // your AI client wrapper
    private final EntityManager entityManager;

    // ---------------- 1. GET FULL WORD ----------------
    @Transactional(readOnly = true)
    public WordDto getFullWord(Long wordId) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new IllegalArgumentException("Word not found"));

        return WordDto.builder()
                .id(word.getId())
                .word(word.getWord())
                .language(word.getLanguage().getCode())
                .level(word.getLevel().name())
                .forms(wordFormRepository.findByWordId(wordId).stream().map(WordForm::getForm).toList())
                .conjugations(wordConjugationRepository.findByWordId(wordId)
                        .stream().collect(Collectors.toMap(WordConjugation::getPerson, WordConjugation::getForm)))
                .examples(wordExampleRepository.findByWordId(wordId).stream().map(WordExample::getSentence).toList())
                .tips(wordTipRepository.findByWordId(wordId).stream().map(WordTip::getTip).toList())
                .synonyms(wordRelationRepository.findSynonyms(wordId).stream().map(Word::getWord).toList())
                .antonyms(wordRelationRepository.findAntonyms(wordId).stream().map(Word::getWord).toList())
                .build();
    }

    // ---------------- 2. ASSIGN WORD TO USER ----------------
    @Transactional
    public void assignWordsToUser(List<Long> wordIdList, UUID userId) {
        for (Long wordId : wordIdList) {
            //query will change for lanugage search
            boolean exists = userWordRepository.existsByUserIdAndWordId(userId, wordId);
            if (!exists) {
                Word wordRef = entityManager.getReference(Word.class, wordId);
                User user = entityManager.getReference(User.class, userId); //makes sense?
                Box box = entityManager.getReference(Box.class, BoxType.INIT.getId());

                UserWord userWord = UserWord.builder()
                        .id(UUID.randomUUID())
                        .user(user)
                        .word(wordRef)
                        .language(wordRef.getLanguage()) // reuse from word
                        .level(wordRef.getLevel())       // reuse from word
                        .box(box)
                        .accuracy(0.0)
                        .lastSeen(Instant.now())
                        .build();

                userWordRepository.save(userWord);
            }
        }
        redisService.addUserAssignedWords(userId, request.getLevel(), request.getSourceLanguage(), wordIdList);
    }

    // ---------------- 3. RANDOM WORDS ----------------
    @Transactional
    public List<WordDto> getRandomWords(UserRandomWordRequest request) {
        UUID userId = request.getUserId();
        String level = request.getLevel();
        String language = request.getSourceLanguage();
        int count = request.getCount();

        // Step 1: Redis unseen
        List<Long> redisWordIds = redisService.getRandomUnseenWordsForUser(userId, language, level, count);

        // Step 2: DB fallback if needed
        int remaining = count - redisWordIds.size();
        List<Long> dbWordIds = remaining > 0
                ? wordRepository.findRandomUnassignedWords(userId, level, remaining)
                : List.of();

        // Step 3: Aggregate so far
        List<Long> allIds = new ArrayList<>();
        allIds.addAll(redisWordIds);
        allIds.addAll(dbWordIds);

        // Step 4: If still missing → call LLM
        List<WordDto> llmWords = new ArrayList<>();
        if (allIds.size() < count) {
            int needed = count - allIds.size();

            // call LLM for fresh words
            llmWords = llmService.generateWords(level, needed);

            // save and assign
            llmWords.forEach(word -> saveAndAssignWord(userId, word));

            // also add to Redis level pool
            redisService.addLevelWordIds(level, llmWords.stream().map(WordDto::getId).toList());
        }

        // Final: persist assignment of DB/Redis words
        if (!allIds.isEmpty()) {
            assignWordsToUser(allIds);
        }

        // Convert to DTOs
        Stream<WordDto> dbAndRedisDtos = allIds.stream().map(this::getFullWord);
        return Stream.concat(dbAndRedisDtos, llmWords.stream()).toList();
    }


    // ---------------- 4. SAVE LLM WORD ----------------
    @Transactional
    public void saveAndAssignWord(WordDto wordDto) {
        UUID userId = getCurrentUserId();
        Language language = languageRepository.findByCode(languageCode)
                .orElseThrow(() -> new IllegalArgumentException("Language not supported: " + languageCode));

        Word word = wordRepository.findByWordAndLanguage(wordDto.getWord(), wordDto.getLanguage())
                .orElseGet(() -> {
                    Word newWord = new Word();
                    newWord.setWord(wordDto.getWord());
                    newWord.setLevel(wordDto.getLevel());
                    return wordRepository.save(newWord);
                });

        Long wordId = word.getId();

        // Step 2: Save forms (V2, V3, Future, etc.)
        if (wordDto.getForms() != null) {
            for (String form : wordDto.getForms()) {
                if (wordFormRepository.findByForm(form).isEmpty()) {
                    WordForm wf = new WordForm();
                    wf.setWordId(wordId);
                    wf.setForm(form);
                    wf.setFormType(detectFormType(form, wordDto)); // helper
                    wordFormRepository.save(wf);
                }
            }
        }

        // Step 3: Save conjugations
        if (wordDto.getConjugations() != null) {
            wordDto.getConjugations().forEach((person, form) -> {
                if (wordConjugationRepository.findByForm(form).isEmpty()) {
                    WordConjugation wc = new WordConjugation();
                    wc.setWord(word);
                    wc.setPerson(person);
                    wc.setForm(form);
                    wordConjugationRepository.save(wc);
                }
            });
        }

        // Step 4: Synonyms & Antonyms
        if (wordDto.getSynonyms() != null) {
            for (String syn : wordDto.getSynonyms()) {
                Word synonymWord = wordRepository.findByWord(syn)
                        .orElseGet(() -> wordRepository.save(new Word(syn, wordDto.getLevel())));
                wordRelationRepository.save(new WordRelation(wordId, synonymWord.getId(), "SYNONYM"));
            }
        }

        if (wordDto.getAntonyms() != null) {
            for (String ant : wordDto.getAntonyms()) {
                Word antonymWord = wordRepository.findByWord(ant)
                        .orElseGet(() -> wordRepository.save(new Word(ant, wordDto.getLevel())));
                wordRelationRepository.save(new WordRelation(wordId, antonymWord.getId(), "ANTONYM"));
            }
        }

        // Step 5: Assign to user
        assignWordsToUser(UserWordRequest.builder()
                .userId(userId)
                .wordIds(List.of(wordId))
                .build());

    }
// logic for translation requests
    @Transactional
    public WordDto translateWord(String inputWord, String sourceLangCode, String targetLangCode) {
        // 1️⃣ Redis lookup first
        Optional<WordDto> cached = redisService.getCachedTranslation(sourceLangCode, targetLangCode, inputWord);
        if (cached.isPresent()) return cached.get();

        // 2️⃣ Load language entities
        //better check method??
        Language sourceLang = languageRepository.findByCode(sourceLangCode)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported source language: " + sourceLangCode));
        Language targetLang = languageRepository.findByCode(targetLangCode)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported target language: " + targetLangCode));

        // 3️⃣ Try to find the word in DB (exact, forms, conjugations)
        Optional<WordDto> sourceWordOpt = searchAndReturnWord(inputWord);
        if (sourceWordOpt.isEmpty()) {
            throw new IllegalArgumentException("Source word not found in " + sourceLangCode + ": " + inputWord);
        }
        Word sourceWord = wordRepository.findById(sourceWordOpt.get().getId()).orElseThrow();

        // 4️⃣ Check existing translation mapping
        Optional<WordTranslation> translationOpt = wordTranslationRepository.findTranslation(sourceWord, sourceLang, targetLang);
        if (translationOpt.isPresent()) {
            Word targetWord = translationOpt.get().getTargetWord();
            WordDto targetDto = getFullWord(targetWord.getId());
            redisService.cacheTranslation(sourceLangCode, targetLangCode, inputWord, targetDto);
            return targetDto;
        }

        // 5️⃣ No translation found → check if word exists in target language DB
        Optional<WordDto> targetFromDb = wordRepository.findByWordAndLanguage(inputWord, targetLang)
                .map(w -> getFullWord(w.getId()));

        if (targetFromDb.isPresent()) {
            cacheAndLinkTranslation(sourceWord, targetFromDb.get(), sourceLang, targetLang, inputWord);
            return targetFromDb.get();
        }

        // 6️⃣ Fallback to LLM (generate translation)
        //Add prompt builder for translation
        WordDto translated = llmService.generateTranslatedWord(inputWord, sourceLangCode, targetLangCode);

        // 7️⃣ Save translated word + link
        Word targetWord = saveAndAssignWord(translated);
        wordTranslationRepository.save(
                WordTranslation.builder()
                        .sourceWord(sourceWord)
                        .targetWord(targetWord)
                        .sourceLanguage(sourceLang)
                        .targetLanguage(targetLang)
                        .createdAt(Instant.now())
                        .build()
        );

        // 8️⃣ Cache it
        redisService.cacheTranslation(sourceLangCode, targetLangCode, inputWord, translated);

        return translated;
    }

    private void cacheAndLinkTranslation(Word sourceWord, WordDto targetDto,
                                         Language srcLang, Language tgtLang, String inputWord) {
        Word targetWord = wordRepository.findById(targetDto.getId()).orElseThrow();
        wordTranslationRepository.save(
                WordTranslation.builder()
                        .sourceWord(sourceWord)
                        .targetWord(targetWord)
                        .sourceLanguage(srcLang)
                        .targetLanguage(tgtLang)
                        .createdAt(Instant.now())
                        .build()
        );
        redisService.cacheTranslation(srcLang.getCode(), tgtLang.getCode(), inputWord, targetDto);
    }

    // ---------------- 5. SEARCH ----------------
    //extend for language parameter //also with level?
    //or we can have two different methods? one for search and one for translation
    @Transactional(readOnly = true)
    public Optional<WordDto> searchAndReturnWord(String input) {
        // Step 1: Check in main word table
        Optional<Word> baseWord = wordRepository.findByWord(input);
        if (baseWord.isPresent()) {
            return Optional.of(getFullWord(baseWord.get().getId()));
        }

        // Step 2: Check in word_forms (V2, V3, Future)
        Optional<WordForm> form = wordFormRepository.findByForm(input);
        if (form.isPresent()) {
            return Optional.of(getFullWord(form.get().getId()));
        }

        // Step 3: Check in word_conjugations
        Optional<WordConjugation> conj = wordConjugationRepository.findByForm(input);
        if (conj.isPresent()) {
            return Optional.of(getFullWord(conj.get().getWord().getId()));
        }

        // Step 4: Not found → let LLM handle
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public Optional<UserWord> findUserWord(UUID userId, Long wordId) {
        return userWordRepository.findByUserIdAndWordId(userId, wordId);
    }

    @Transactional
    public void updateUserWordBox(UserWord userWord, Box newBox) {
        userWord.setBox(newBox);
        userWord.setLastSeen(LocalDateTime.now());
        userWord.setNextReview(LocalDateTime.now().plusDays(newBox.getIntervalDays()));
        userWordRepository.save(userWord);
    }

    @Transactional
    public void createUserWord(User user, Word word, Box initBox) {
        UserWord userWord = UserWord.builder()
                .user(user)
                .word(word)
                .language(word.getLanguage())
                .level(word.getLevel())
                .box(initBox)
                .lastSeen(LocalDateTime.now())
                .nextReview(LocalDateTime.now().plusDays(initBox.getIntervalDays()))
                .build();

        userWordRepository.save(userWord);
    }

    @Transactional(readOnly = true)
    public List<UserWord> findDueWords(Long userId, LocalDateTime now) {
        return userWordRepository.findDueWords(userId, now);
    }
}
