package com.aroha.mutualfund.pojo;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldingTransactions {

	private int holdingId;
    private LocalDate dateOfPortfolio;
    private int quantity;
    private BigDecimal marketValue;
    private BigDecimal netAsset;
    private LocalDate createdDate;
    private String createdBy;
    private LocalDate updatedAt;
    private String updatedBy;
    

}
