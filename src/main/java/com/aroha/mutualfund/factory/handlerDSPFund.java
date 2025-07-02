package com.aroha.mutualfund.factory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.*;

import com.aroha.mutualfund.dto.EquityDTO;
import com.aroha.mutualfund.dto.MutualFundDTO;

public class handlerDSPFund implements MutualFundFile {

	private static final Pattern DATE_PREFIX_PATTERN = Pattern.compile(".*[Aa]s [Oo]n");

	@Override
	public MutualFundDTO extractFile(Sheet sheet){
		if (sheet == null) {
			throw new IllegalArgumentException("Sheet cannot be null");
		}

		MutualFundDTO mutualFundDTO = new MutualFundDTO();
		List<EquityDTO> listEquity = new ArrayList<>();

		// Step 1: Get basic fund information
		String fundName = getCellStringValue(sheet.getRow(0), 1);
		String dateText = getCellStringValue(sheet.getRow(1), 1);
		LocalDate portfolioDate = parsePortfolioDate(dateText);
		String fundType = getFundType(fundName);

		mutualFundDTO.setFundName(fundName);
		mutualFundDTO.setFundType(fundType);
		mutualFundDTO.setDateOfPortfolio(portfolioDate);

		// Step 2: Process each row of holdings data
		for (int rowNum = 7; rowNum <= sheet.getLastRowNum(); rowNum++) {
			Row row = sheet.getRow(rowNum);
			if (row == null)
				continue;

			// Check for "Total" row to stop processing
			Cell cell = row.getCell(1);
			if (cell != null && "Total".equalsIgnoreCase(cell.getStringCellValue().trim())) {
				break;
			}

			// Get instrument details
			String instrumentName = getCellStringValue(row, 1);
			String isinCode = getCellStringValue(row, 2);
			String sector = getCellStringValue(row, 3);

			// Validate required fields
			if (instrumentName.isEmpty() || isinCode.isEmpty() || sector.isEmpty()) {
				throw new IllegalStateException("Missing required fields in row " + rowNum + ": instrumentName="
						+ instrumentName + ", isinCode=" + isinCode + ", sector=" + sector);
			}

			// Get numeric values with validation
			int quantity;
			try {
				quantity = (int) getCellNumericValue(row, 4);
			} catch (NumberFormatException e) {
				throw new IllegalStateException("Invalid quantity value in row " + rowNum, e);
			}

			BigDecimal marketValue;
			try {
				String rawMarketValue = getCellStringValue(row, 5);
				marketValue = new BigDecimal(rawMarketValue);
			} catch (NumberFormatException e) {
				throw new IllegalStateException("Invalid market value in row " + rowNum, e);
			}

			BigDecimal netAssetPercentage;
			try {
				double rawNetAsset = getCellNumericValue(row, 6);
				netAssetPercentage = BigDecimal.valueOf(rawNetAsset).multiply(BigDecimal.valueOf(100));
			} catch (NumberFormatException e) {
				throw new IllegalStateException("Invalid net asset value in row " + rowNum, e);
			}

			EquityDTO equityDTO = new EquityDTO();
			equityDTO.setInstrumentName(instrumentName);
			equityDTO.setIsin(isinCode);
			equityDTO.setSector(sector);
			equityDTO.setQuantity(quantity);
			equityDTO.setMarketValue(marketValue);
			equityDTO.setNetAsset(netAssetPercentage);

			listEquity.add(equityDTO);
		}

		mutualFundDTO.setEquity(listEquity);
		return mutualFundDTO;
	}

	private String getCellStringValue(Row row, int columnIndex) throws IllegalStateException {
		if (row == null) {
			throw new IllegalArgumentException("Row cannot be null");
		}

		Cell cell = row.getCell(columnIndex);
		if (cell == null) {
			throw new IllegalStateException("Cell at column " + columnIndex + " is null");
		}

		try {
			switch (cell.getCellType()) {
			case STRING:
				return cell.getStringCellValue().trim();
			case NUMERIC:
				// For numeric cells that should be treated as strings
				return String.valueOf((long) cell.getNumericCellValue());
			default:
				throw new IllegalStateException("Unsupported cell type at column " + columnIndex);
			}
		} catch (Exception e) {
			throw new IllegalStateException("Error reading cell value at column " + columnIndex, e);
		}
	}

	private double getCellNumericValue(Row row, int columnIndex) throws IllegalStateException {
		if (row == null) {
			throw new IllegalArgumentException("Row cannot be null");
		}

		Cell cell = row.getCell(columnIndex);
		if (cell == null) {
			throw new IllegalStateException("Cell at column " + columnIndex + " is null");
		}

		try {
			switch (cell.getCellType()) {
			case NUMERIC:
				return cell.getNumericCellValue();
			case STRING:
				String value = cell.getStringCellValue().trim();
				if (value.isEmpty()) {
					throw new NumberFormatException("Empty string value");
				}
				return Double.parseDouble(value);
			default:
				throw new IllegalStateException("Unsupported cell type at column " + columnIndex);
			}
		} catch (NumberFormatException e) {
			throw new IllegalStateException("Invalid numeric value at column " + columnIndex, e);
		}
	}

	private LocalDate parsePortfolioDate(String dateText) throws DateTimeParseException {
		if (dateText == null || dateText.trim().isEmpty()) {
			throw new IllegalArgumentException("Date text cannot be null or empty");
		}

		// Remove any prefix like "Portfolio as on", "Report as on" etc.
		String cleanDateText = DATE_PREFIX_PATTERN.matcher(dateText).replaceFirst("").trim();

		// Try common date formats
		DateTimeFormatter[] formatters = { DateTimeFormatter.ofPattern("MMMM d, yyyy"), // "June 30, 2023"
				DateTimeFormatter.ofPattern("MMM d, yyyy"), // "Jun 30, 2023"
				DateTimeFormatter.ofPattern("d MMMM, yyyy"), // "30 June, 2023"
				DateTimeFormatter.ofPattern("yyyy-MM-dd") // "2023-06-30"
		};

		for (DateTimeFormatter formatter : formatters) {
			try {
				return LocalDate.parse(cleanDateText, formatter);
			} catch (DateTimeParseException e) {
				// Try next format
			}
		}

		throw new DateTimeParseException("Unable to parse date: " + dateText, dateText, 0);
	}

	private String getFundType(String fundName) throws IllegalArgumentException {
		if (fundName == null || fundName.trim().isEmpty()) {
			throw new IllegalArgumentException("Fund name cannot be null or empty");
		}

		String upperName = fundName.toUpperCase();
		if (upperName.contains("MID CAP"))
			return "Mid Cap";
		if (upperName.contains("SMALL CAP"))
			return "Small Cap";
		if (upperName.contains("LARGE CAP"))
			return "Large Cap";
		return "Other";
	}
}