package com.aroha.mutualfund.pojo;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Fund {
	private String fundName;
    private String fundType;
    private LocalDate createdDate;
    private String createdBy;
    private LocalDate updatedAt;
    private String updatedBy;
}
