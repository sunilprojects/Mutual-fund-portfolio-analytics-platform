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
public class InstrumentRepository {

	private final JdbcTemplate jdbcTemplate;

	//Constructor injection
	public InstrumentRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	//To check and insert instrument details to instrument tables
	public int insertInstrumentIfNotExists(String isin, String instrumentName, String sector, String createdBy) {
		// Check if instrument already exists by ISIN (unique)
		String checkSql = "SELECT instrument_id FROM instrument WHERE isin = ?";

		List<Integer> results = jdbcTemplate.query(checkSql, (rs, rowNum) -> rs.getInt("instrument_id"), isin);

		if (!results.isEmpty()) {
			return results.get(0);
		}

		// Insert new instrument
		String sql = "INSERT INTO instrument (isin, instrument_name, sector, created_date, created_by, updated_at, updated_by) "
				+ "VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)";

		//to store id after inserting
		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, isin);
			ps.setString(2, instrumentName);
			ps.setString(3, sector);
			ps.setString(4, createdBy);
			ps.setString(5, createdBy);
			return ps;
		}, keyHolder);

		return Objects.requireNonNull(keyHolder.getKey()).intValue();
	}
	
    public List<String> findSectorsByFundId(int fundId) {
        String sql = "SELECT i.sector " +
                     "FROM instrument i " +
                     "JOIN holdings h ON h.instrument_id = i.instrument_id " +
                     "JOIN fund f ON f.fund_id = h.fund_id " +
                     "WHERE f.fund_id = ?";
        return jdbcTemplate.queryForList(sql, String.class, fundId);
    }
}
