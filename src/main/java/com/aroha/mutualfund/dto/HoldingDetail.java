package com.aroha.mutualfund.dto;

import java.math.BigDecimal;
import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HoldingDetail {

	private String instrumentName;
	private String isin;
	private String sector;
	private Date date;
	private int quantity;
	private BigDecimal marketValue;
	private BigDecimal percentageOfFund;
}
