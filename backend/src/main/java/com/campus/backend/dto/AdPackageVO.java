package com.campus.backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AdPackageVO {

    private String id;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer exposureBoost;

    private Integer durationDays;

    private String badge;

    private String estimatedReach;

    private String scenario;

    private String terms;
}
