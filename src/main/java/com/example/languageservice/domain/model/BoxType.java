package com.example.languageservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public enum BoxType {
    INIT(1, "init", 2),
    KNOWN_3DAYS(2, "known-3days", 3),
    KNOWN_1WEEK(3, "known-1week", 7),
    KNOWN_2WEEKS(4, "known-2weeks", 14),
    KNOWN_1MONTH(5, "known-1month", 30),
    KNOWN_3MONTHS(6, "known-3months", 90),
    UNKNOWN_1DAY(7, "unknown-1day", 1);

    private final int id;           // Matches DB box_id
    private final String name;      // Human-readable
    private final int intervalDays; // For scheduling

    public static final Map<Integer, BoxType> nextBoxMap = Map.of(
            BoxType.INIT.getId(), BoxType.KNOWN_3DAYS,
            BoxType.KNOWN_3DAYS.getId(), BoxType.KNOWN_1WEEK,
            BoxType.KNOWN_1WEEK.getId(), BoxType.KNOWN_2WEEKS,
            BoxType.KNOWN_2WEEKS.getId(), BoxType.KNOWN_1MONTH,
            BoxType.KNOWN_1MONTH.getId(), BoxType.KNOWN_3MONTHS
    );

}
