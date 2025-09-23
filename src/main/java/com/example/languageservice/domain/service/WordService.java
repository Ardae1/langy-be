package com.example.languageservice.domain.service;

import com.example.languageservice.api.dto.WordEnhanceResponse;
import com.example.languageservice.api.dto.WordExampleDto;
import com.example.languageservice.domain.model.Word;
import com.example.languageservice.domain.model.WordExample;
import com.example.languageservice.domain.repository.WordExampleRepository;
import com.example.languageservice.domain.repository.WordRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WordService {

    private static final Logger log = LoggerFactory.getLogger(WordService.class);
    private static final String SYSTEM_PROMPT = "You are an advanced German language teaching assistant with expertise in linguistics, pedagogy. Respond only in the following format:\n" +
            "### <Word>\n" +
            "V2: <Past Tense Form>\n" +
            "V3: <Past Participle Form>\n" +
            "Future: <Future Tense Form>\n" +
            "Predicate Inflections:\n" +
            "ich: <Conjugated Form for 'ich'>\n" +
            "du: <Conjugated Form for 'du'>\n" +
            "er/sie/es: <Conjugated Form for 'er/sie/es'>\n" +
            "wir: <Conjugated Form for 'wir'>\n" +
            "ihr: <Conjugated Form for 'ihr'>\n" +
            "sie/Sie: <Conjugated Form for 'sie/Sie'>\n" +
            "Examples:\n" +
            "- Ich <Word> <Example Sentence for 'ich'>\n" +
            "- Du <Word> <Example Sentence for 'du'>\n" +
            "- Er/Sie/Es <Word> <Example Sentence for 'er/sie/es'>\n" +
            "- Wir <Word> <Example Sentence for 'wir'>\n" +
            "- Ihr <Word> <Example Sentence for 'ihr'>\n" +
            "- Sie/Sie <Word> <Example Sentence for 'sie/Sie'>\n" +
            "V2 Examples:\n" +
            "- Ich <V2 Form> <Example Sentence for 'ich' with V2>\n" +
            "- Du <V2 Form> <Example Sentence for 'du' with V2>\n" +
            "- Er/Sie/Es <V2 Form> <Example Sentence for 'er/sie/es' with V2>\n" +
            "- Wir <V2 Form> <Example Sentence for 'wir' with V2>\n" +
            "- Ihr <V2 Form> <Example Sentence for 'ihr' with V2>\n" +
            "- Sie/Sie <V2 Form> <Example Sentence for 'sie/Sie' with V2>\n" +
            "V3 Examples:\n" +
            "- Ich <V3 Form> <Example Sentence for 'ich' with V3>\n" +
            "- Du <V3 Form> <Example Sentence for 'du' with V3>\n" +
            "- Er/Sie/Es <V3 Form> <Example Sentence for 'er/sie/es' with V3>\n" +
            "- Wir <V3 Form> <Example Sentence for 'wir'>\n" +
            "- Ihr <V3 Form> <Example Sentence for 'ihr'>\n" +
            "- Sie/Sie <V3 Form> <Example Sentence for 'sie/Sie'>\n" +
            "Second-Language Acquisition Method:\n" +
            "- <provide comprehensive and practical language examples using the word provided. Use the '1+2 approach' to create examples that help learners effectively internalize the word, its forms, and usage> \n" +
            "Learning Tips:\n" +
            "- <give info if there is any special condition or exception regarding the word>.\n" +
            "Synonyms:\n" +
            "- <Synonym 1>: <Example Sentence for Synonym 1>\n" +
            "- <Synonym 2>: <Example Sentence for Synonym 2>\n" +
            "Antonyms:\n" +
            "- <Antonym 1>: <Example Sentence for Antonym 1>\n" +
            "- <Antonym 2>: <Example Sentence for Antonym 2>";

    private final WordRepository wordRepository;
    private final WordExampleRepository wordExampleRepository;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String llmApiUrl;
    private final String apiKey;


    public WordService(WordRepository wordRepository,
                       WordExampleRepository wordExampleRepository,
                       HttpClient httpClient,
                       ObjectMapper objectMapper,
                       @Value("${llm.api.url}") String llmApiUrl,
                       @Value("${llm.api.key}") String apiKey) {
        this.wordRepository = wordRepository;
        this.wordExampleRepository = wordExampleRepository;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.llmApiUrl = llmApiUrl;
        this.apiKey = apiKey;
    }

    @Transactional
    @Cacheable(value = "wordEnhancements", key = "#wordText")
    public WordEnhanceResponse getEnhancedWord(String wordText) {
        // 1. Check database for existing word data
        Optional<Word> existingWordOpt = wordRepository.findByWordText(wordText);

        if (existingWordOpt.isPresent()) {
            Word existingWord = existingWordOpt.get();
            log.info("Returning cached word data for: {}", wordText);
            return mapToResponse(existingWord);
        }

        // 2. Call LLM to generate data
        log.info("Calling LLM to generate enhanced word data for: {}", wordText);
        String prompt = SYSTEM_PROMPT.replace("<Word>", wordText);
        String rawResponse = null;
        try {
            rawResponse = callLlmApi(prompt);
            return processAndSaveWord(wordText, rawResponse);
        } catch (IOException | InterruptedException e) {
            log.error("Failed to generate or parse word enhancement for {}: {}", wordText, e.getMessage());
            throw new RuntimeException("Failed to get enhanced word data from AI.", e);
        }
    }

    private WordEnhanceResponse processAndSaveWord(String wordText, String rawResponse) throws IOException {
        // Parse the raw response and save to database
        Word newWord = new Word();
        newWord.setWordText(wordText);
        newWord.setLanguage("de");

        JsonNode rootNode = objectMapper.readTree(rawResponse);
        newWord.setV2(rootNode.path("V2").asText());
        newWord.setV3(rootNode.path("V3").asText());
        newWord.setFutureTense(rootNode.path("Future").asText());

        // This requires careful parsing of the LLM's structured text output
        // For simplicity, we assume a JSON object in the future
        // newWord.setPredicateInflections(rootNode.path("Predicate Inflections"));
        // newWord.setSynonyms(parseList(rootNode.path("Synonyms")));
        // newWord.setAntonyms(parseList(rootNode.path("Antonyms")));

        wordRepository.save(newWord);

        // Process and save examples
        // This is a simplified example, a real implementation would be more robust
        List<WordExample> examples = new ArrayList<>();
        // Save examples based on the parsed response
        wordExampleRepository.saveAll(examples);

        return mapToResponse(newWord);
    }

    private WordEnhanceResponse mapToResponse(Word word) {
        List<WordExampleDto> exampleDtos = wordExampleRepository.findByWordId(word.getId()).stream()
                .map(example -> new WordExampleDto(example.getExampleText(), example.getExampleType(), example.getPersona()))
                .collect(Collectors.toList());

        List<String> synonyms = word.getSynonyms() != null ? word.getSynonyms() : new ArrayList<>();
        List<String> antonyms = word.getAntonyms() != null ? word.getAntonyms() : new ArrayList<>();

        return new WordEnhanceResponse(word.getWordText(), word.getV2(), word.getV3(), word.getFutureTense(), word.getPredicateInflections(), synonyms, antonyms, exampleDtos, null);
    }

    private String callLlmApi(String prompt) throws IOException, InterruptedException {
        String jsonPayload = String.format("{\"prompt\": \"%s\"}", prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(llmApiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("LLM API returned non-200 status: {}", response.statusCode());
            log.error("Response body: {}", response.body());
            throw new RuntimeException("LLM API call failed with status code: " + response.statusCode());
        }

        JsonNode rootNode = objectMapper.readTree(response.body());
        return rootNode.path("response").asText();
    }
}
