package com.clinique.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor // Pratique pour construire l'objet
public class PredictionRequest {

    // Doit correspondre exactement aux noms de l'API Python
    @JsonProperty("lead_time_days")
    private double leadTimeDays;

    @JsonProperty("day_of_week")
    private int dayOfWeek;

    @JsonProperty("hour_of_day")
    private int hourOfDay;
}