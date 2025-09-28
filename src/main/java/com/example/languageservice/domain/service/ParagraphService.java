package com.example.languageservice.domain.service;

import com.example.languageservice.api.dto.ParagraphRequest;
import com.example.languageservice.api.dto.ParagraphResponse;
import com.example.languageservice.domain.model.AiPromptPayload;
import com.example.languageservice.domain.model.Paragraph;
import com.example.languageservice.domain.model.UserParagraph;
import com.example.languageservice.domain.repository.ParagraphRepository;
import com.example.languageservice.domain.repository.UserParagraphRepository;
import com.example.languageservice.domain.utils.PromptBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

import static com.example.languageservice.security.SecurityUtils.getCurrentUserId;

//currently data is mocked ->  should be integrated with an actual LLM (Gemma 2b) to generate paragraphs based on the request.
//based on response from gemma 2b, buffer mechanism might be needed.
//remove validation from service and do it in controller.
//if user vocabulary is to be included, it should be selecting %20 of the words from user vocabulary randomly and included into LLM prompt. Rest comes from LLM response.
//we should also check this based on selected difficulty level and paragraph length requested by user. %20 should be defined accordingly and if users have less words, we should take all of them.
//we should be also providing the words that used from user vocabulary in the response so that user can see which words were used.
// If there is no word in user vocabulary, or in requested level. We should not include that parameter in LLM prompt and user should be informed regarding this.
//Users can add words to their vocabulary list from the generated paragraph if they want to. -> word service takes care of that functionality.
// for paragraph generation, each paragraph generated should be stored in database in a centralized point and associated with user. -> maybe having a special logic to return the paragraph randomly from database rather than sending call to LLM first.
//Also for the paragraph generation, we can somehow manage to return an existing paragraph from database if it matches the request of user for words if userwords included. This way we can reduce the cost of LLM calls.
@Service
public class ParagraphService {

    private final ParagraphRepository paragraphRepository;
    private final UserParagraphRepository userParagraphRepository;
    private final RedisService redisService;
    private final LlmService llmService;

    public ParagraphService(ParagraphRepository paragraphRepository,
                            UserParagraphRepository userParagraphRepository,
                            RedisService redisService,
                            LlmService llmService) {
        this.paragraphRepository = paragraphRepository;
        this.userParagraphRepository = userParagraphRepository;
        this.redisService = redisService;
        this.llmService = llmService;
    }

    @Transactional
    public ParagraphResponse generateParagraph(ParagraphRequest request) throws IOException, InterruptedException {
        UUID userId = getCurrentUserId();
        Set<String> words = new HashSet<>(request.getSelectedWords());

        // 1. Redis superset lookup
        Set<String> paraIds = redisService.intersectWords(words);
        if (paraIds != null && !paraIds.isEmpty()) {
            Long paraId = Long.valueOf(paraIds.iterator().next().replace("para:", ""));
            ParagraphResponse cached = redisService.getParagraph(paraId);
            if (cached != null) {
                return linkUser(userId, paraId, cached, request.getSelectedWords());
            }
        }

        // 2. Postgres superset search (on user_paragraphs)
        List<Paragraph> supersetMatches = userParagraphRepository.findSupersetMatch(words.toArray(new String[0]));
        if (!supersetMatches.isEmpty()) {
            Paragraph para = supersetMatches.get(0);
            return linkUser(userId, para.getId(), toParagraphResponse(para, request.getSelectedWords()), request.getSelectedWords());
        }

        // 3. Postgres full-text search
        String tsQuery = String.join(" & ", words);
        List<Paragraph> contentMatches = paragraphRepository.searchByContent(tsQuery);
        if (!contentMatches.isEmpty()) {
            Paragraph para = contentMatches.get(0);
            return linkUser(userId, para.getId(), toParagraphResponse(para, request.getSelectedWords()), request.getSelectedWords());
        }

        // 4. LLM fallback
        AiPromptPayload paragraphPayload = PromptBuilder.buildParagraphPrompt(request);
        ParagraphResponse llmResponse = llmService.generateContent(paragraphPayload);

        Paragraph paragraph = Paragraph.builder()
                .content(llmResponse.getText())
                .level(request.getLanguageLevel())
                .topic(request.getTopic())
                .length(request.getLength())
                .build();
        paragraphRepository.save(paragraph);

        redisService.saveParagraphIndex(paragraph.getId(), llmResponse.getUsedWords());
        redisService.saveParagraphContent(paragraph.getId(), llmResponse);

        return linkUser(userId, paragraph.getId(), llmResponse, llmResponse.getUsedWords());
    }

    private ParagraphResponse toParagraphResponse(Paragraph paragraph, List<String> words) {
        return ParagraphResponse.builder()
                .text(paragraph.getContent())
                .usedWords(words)
                .difficulty(paragraph.getLevel().name())
                .topic(paragraph.getTopic())
                .build();
    }

    private ParagraphResponse linkUser(UUID userId, Long paraId, ParagraphResponse response, List<String> wordsUsed) {
        UserParagraph userParagraph = UserParagraph.builder()
                .userId(userId)
                .paragraph(Paragraph.builder().id(paraId).build())
                .requestedWords(wordsUsed)
                .build();
        userParagraphRepository.save(userParagraph);
        return response;
    }
}
