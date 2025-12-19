package com.clinique.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data // requis pour que Jackson puisse setter les champs
public class PredictionResponse {

    // Doit correspondre exactement au nom de l'API Python
    @JsonProperty("cancellation_risk_score")
    private Double cancellationRiskScore;
}