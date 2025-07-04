package com.aroha.mutualfund.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class HoldingTransactionsRepository {

	private final JdbcTemplate jdbcTemplate;

	//Constructor injection
	public HoldingTransactionsRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	//Inserting if row does not exist
	//Updating if row exists
	//Checking for duplication based on holding_id, date_of_portfolio 
	public void upsertTransaction(int holdingId, LocalDate dateOfPortfolio, int quantity, BigDecimal marketValue,
			BigDecimal netAsset, String updatedBy) {
		String sql = "INSERT INTO holding_transactions "
				+ "(holding_id, date_of_portfolio, quantity, market_value, net_asset, "
				+ "created_date, created_by, updated_at, updated_by) "
				+ "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?) " + "ON DUPLICATE KEY UPDATE "
				+ "quantity = VALUES(quantity), " + "market_value = VALUES(market_value), "
				+ "net_asset = VALUES(net_asset), " + "updated_at = CURRENT_TIMESTAMP, " + "updated_by = VALUES(updated_by)";

		jdbcTemplate.update(sql, holdingId, dateOfPortfolio, quantity, marketValue, netAsset, updatedBy, updatedBy);
	}

}
