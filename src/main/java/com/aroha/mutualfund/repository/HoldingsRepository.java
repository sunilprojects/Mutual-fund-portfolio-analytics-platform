package com.aroha.mutualfund.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class HoldingsRepository {

	private final JdbcTemplate jdbcTemplate;

	public HoldingsRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public int insertHoldingIfNotExists(int fundId, int instrumentId, String createdBy) {
		// Check if holding already exists
		String checkSql = "SELECT holding_id FROM holdings WHERE fund_id = ? AND instrument_id = ?";
		List<Integer> results = jdbcTemplate.query(checkSql, (rs, rowNum) -> rs.getInt("holding_id"), fundId,
				instrumentId);

		if (!results.isEmpty()) {
			return results.get(0);
		}

		// Insert new holding
		String sql = "INSERT INTO holdings (fund_id, instrument_id) VALUES (?, ?)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, fundId);
			ps.setInt(2, instrumentId);
			return ps;
		}, keyHolder);

		return Objects.requireNonNull(keyHolder.getKey()).intValue();
	}
}
