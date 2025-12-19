package com.clinique.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PredictionTimingRequest {

    @JsonProperty("last_progress_score")
    private int lastProgressScore;
}