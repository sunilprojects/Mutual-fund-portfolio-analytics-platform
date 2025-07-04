package com.aroha.mutualfund.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.aroha.mutualfund.dto.HoldingDetail;
import com.aroha.mutualfund.service.impl.MutualFundServiceImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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

	public List<HoldingDetail> getHoldingsByFundId(int fundId) {
		String sql = """
				SELECT
		           i.instrument_name,
		           i.isin,
		           i.sector,
		           ht.date_of_portfolio as date,
		           ht.quantity,
		           ht.market_value,
		           ht.net_asset 
		       FROM
		           fund f
		       JOIN
		           holdings h ON f.fund_id = h.fund_id
		       JOIN
		           instrument i ON h.instrument_id = i.instrument_id
		       JOIN
		           holding_transactions ht ON h.holding_id = ht.holding_id
		       WHERE
		           f.fund_id = ?   """;

		return jdbcTemplate.query(sql, new Object[] { fundId },
				(rs, rowNum) -> new HoldingDetail(rs.getString("instrument_name"), rs.getString("isin"),
						rs.getString("sector"), rs.getDate("date"), rs.getInt("quantity"),
						rs.getBigDecimal("market_value"), rs.getBigDecimal("net_asset")));
	}
}
