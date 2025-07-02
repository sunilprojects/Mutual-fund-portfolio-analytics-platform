package com.aroha.mutualfund.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class HoldingTransactionsRepository {

	private final JdbcTemplate jdbcTemplate;

	public HoldingTransactionsRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void upsertTransaction(int holdingId, LocalDate dateOfPortfolio, int quantity, BigDecimal marketValue,
			BigDecimal netAsset, String updatedBy) {
		String sql = "INSERT INTO holding_transactions "
				+ "(holding_id, date_of_portfolio, quantity, market_value, net_asset, "
				+ "created_date, created_by, updated_at, updated_by) "
				+ "VALUES (?, ?, ?, ?, ?, CURRENT_DATE, ?, CURRENT_DATE, ?) " + "ON DUPLICATE KEY UPDATE "
				+ "quantity = VALUES(quantity), " + "market_value = VALUES(market_value), "
				+ "net_asset = VALUES(net_asset), " + "updated_at = CURRENT_DATE, " + "updated_by = VALUES(updated_by)";

		jdbcTemplate.update(sql, holdingId, dateOfPortfolio, quantity, marketValue, netAsset, updatedBy, updatedBy);
	}

	public void insertTransaction(int holdingId, LocalDate dateOfPortfolio, int quantity, BigDecimal marketValue,
			BigDecimal netAsset, String createdBy) {
		String sql = "INSERT INTO holding_transactions "
				+ "(holding_id, date_of_portfolio, quantity, market_value, net_asset, "
				+ "created_date, created_by, updated_at, updated_by) "
				+ "VALUES (?, ?, ?, ?, ?, CURRENT_DATE, ?, CURRENT_DATE, ?)";

		jdbcTemplate.update(sql, holdingId, dateOfPortfolio, quantity, marketValue, netAsset, createdBy, createdBy);
	}

}
