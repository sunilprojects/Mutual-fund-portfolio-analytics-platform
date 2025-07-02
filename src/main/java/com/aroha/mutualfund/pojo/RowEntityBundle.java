package com.aroha.mutualfund.pojo;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RowEntityBundle {
	private Fund fund;
    private Instrument instrument;
    private Holding holding;
    private HoldingTransactions transaction;
}
