package com.example.languageservice.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UserWordRequest {
    private UUID userId;
    private List<Long> wordIds; // IDs of words to assign
}
