package com.aroha.mutualfund.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FundsResponceDTO {
	private Integer fundId;
	private String fundName;
}
