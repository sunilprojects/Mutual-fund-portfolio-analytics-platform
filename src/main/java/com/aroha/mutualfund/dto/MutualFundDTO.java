package com.aroha.mutualfund.dto;

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
public class MutualFundDTO {

	private String fundName;
    private String fundType;
    private LocalDate dateOfPortfolio;
    private String createdBy;
    private String updatedBy;
    private List<EquityDTO> equity;
    
}
