package com.clinique.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PredictionTimingResponse {

    @JsonProperty("recommended_days_next_session")
    private int recommendedDaysNextSession;
}