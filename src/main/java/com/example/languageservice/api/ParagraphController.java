package com.example.languageservice.api;

import com.example.languageservice.api.dto.ParagraphRequest;
import com.example.languageservice.api.dto.ParagraphResponse;
import com.example.languageservice.domain.service.ParagraphService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/paragraph")
public class ParagraphController {

    private final ParagraphService paragraphService;

    public ParagraphController(ParagraphService paragraphService) {
        this.paragraphService = paragraphService;
    }

    //validation will be done here!! not in service
    @PostMapping("/generate")
    public ResponseEntity<ParagraphResponse> generateParagraph(@RequestBody ParagraphRequest request) {
        ParagraphResponse response = paragraphService.generateParagraph(request);
        return ResponseEntity.ok(response);
    }
}
