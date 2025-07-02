package com.aroha.mutualfund.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EquityDTO {

	private String isin;
    private String instrumentName;
    private String sector;
    private int quantity;
    private BigDecimal marketValue;
    private BigDecimal netAsset;
}
