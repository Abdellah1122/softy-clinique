package com.clinique.api.dto;

import lombok.Data;

@Data
public class ChurnRiskDTO {
    private boolean isChurnRisk;
    private double churnProbability;
}
