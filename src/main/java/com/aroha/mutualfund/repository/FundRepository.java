package com.aroha.mutualfund.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.aroha.mutualfund.dto.FundsResponceDTO;

@Repository
public class FundRepository {

	private final JdbcTemplate jdbcTemplate;

	public FundRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public int insertFundIfNotExists(String fundName, String fundType, String createdBy) {
		// Check if fund already exists
		String checkSql = "SELECT fund_id FROM fund WHERE fund_name = ? AND fund_type = ?";
		List<Integer> results = jdbcTemplate.query(checkSql, (rs, rowNum) -> rs.getInt("fund_id"), fundName, fundType);

		if (!results.isEmpty()) {
			return results.get(0);
		}

		// Insert new fund
		String sql = "INSERT INTO fund (fund_name, fund_type, created_date, created_by, updated_at, updated_by) "
				+ "VALUES (?, ?, CURRENT_DATE, ?, CURRENT_DATE, ?)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, fundName);
			ps.setString(2, fundType);
			ps.setString(3, createdBy);
			ps.setString(4, createdBy);
			return ps;
		}, keyHolder);

		return Objects.requireNonNull(keyHolder.getKey()).intValue();
	}

	public List<FundsResponceDTO> getAllFunds() {
		String sql = "SELECT fund_id,fund_name FROM fund";

		List<FundsResponceDTO> funds = jdbcTemplate.query(sql, (res, row) -> {
			return FundsResponceDTO.builder()
					.fundId(res.getInt("fund_id"))
					.fundName(res.getString("fund_name"))
					.build();
		});

		return funds;
	}
}
