package com.example.languageservice.api;

import com.example.languageservice.api.dto.ParagraphRequest;
import com.example.languageservice.api.dto.ParagraphResponse;
import com.example.languageservice.domain.service.ParagraphService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/paragraph")
public class ParagraphController {

    private final ParagraphService paragraphService;

    public ParagraphController(ParagraphService paragraphService) {
        this.paragraphService = paragraphService;
    }

    //add validation for request body
    @PostMapping("/generate")
    public ResponseEntity<ParagraphResponse> generateParagraph(@RequestBody ParagraphRequest request) throws IOException, InterruptedException {
        ParagraphResponse response = paragraphService.generateParagraph(request);
        return ResponseEntity.ok(response);
    }
}
