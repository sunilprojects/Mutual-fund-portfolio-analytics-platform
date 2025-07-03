package com.aroha.mutualfund.factory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.aroha.mutualfund.dto.EquityDTO;
import com.aroha.mutualfund.dto.MutualFundDTO;

public class HandlerDSPFund implements MutualFundFile {

	private static final Pattern DATE_PREFIX_PATTERN = Pattern.compile(".*[Aa]s [Oo]n");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy"); // "June 30,
																											// 2023"
	private static final int DATA_START_ROW = 7;
	private static final int INSTRUMENT_NAME_COL = 1;
	private static final int ISIN_CODE_COL = 2;
	private static final int SECTOR_COL = 3;
	private static final int QUANTITY_COL = 4;
	private static final int MARKET_VALUE_COL = 5;
	private static final int NET_ASSET_COL = 6;

	@Override
	public MutualFundDTO extractFile(Sheet sheet) {
		if (sheet == null) {
			throw new IllegalArgumentException("Sheet cannot be null");
		}

		MutualFundDTO mutualFundDTO = new MutualFundDTO();
		List<EquityDTO> equityList = new ArrayList<>();

		extractFundInfo(sheet, mutualFundDTO);
		processHoldingsData(sheet, equityList);

		mutualFundDTO.setEquity(equityList);
		return mutualFundDTO;
	}

	private void extractFundInfo(Sheet sheet, MutualFundDTO mutualFundDTO) {
		String fundName = getCellStringValue(sheet.getRow(0), 1);
		LocalDate portfolioDate = parsePortfolioDate(getCellStringValue(sheet.getRow(1), 1));

		mutualFundDTO.setFundName(fundName);
		mutualFundDTO.setFundType(determineFundType(fundName));
		mutualFundDTO.setDateOfPortfolio(portfolioDate);
	}

	private void processHoldingsData(Sheet sheet, List<EquityDTO> equityList) {
		for (int rowNum = DATA_START_ROW; rowNum <= sheet.getLastRowNum(); rowNum++) {
			Row row = sheet.getRow(rowNum);
			if (row == null)
				continue;

			if (isLastEquity(row))
				break;

			EquityDTO equityDTO = createEquityDTO(row);
			if (equityDTO != null) {
				equityList.add(equityDTO);
			}
		}
	}

	private boolean isLastEquity(Row row) {
		Cell cell = row.getCell(INSTRUMENT_NAME_COL);
		return cell != null && "Total".equalsIgnoreCase(cell.getStringCellValue().trim());
	}

	private EquityDTO createEquityDTO(Row row) {
		String instrumentName = getCellStringValue(row, INSTRUMENT_NAME_COL);
		String isinCode = getCellStringValue(row, ISIN_CODE_COL);
		String sector = getCellStringValue(row, SECTOR_COL);
		// Validate required fields - throw exception if any are empty
		if (instrumentName.isEmpty()) {
			throw new IllegalStateException("Missing instrument name in row " + (row.getRowNum() + 1));
		}
		if (isinCode.isEmpty()) {
			throw new IllegalStateException("Missing ISIN code in row " + (row.getRowNum() + 1));
		}
		if (sector.isEmpty()) {
			throw new IllegalStateException("Missing sector in row " + (row.getRowNum() + 1));
		}
		try {
			EquityDTO equityDTO = new EquityDTO();
			equityDTO.setInstrumentName(instrumentName);
			equityDTO.setIsin(isinCode);
			equityDTO.setSector(sector);
			equityDTO.setQuantity((int) getCellNumericValue(row, QUANTITY_COL));
			equityDTO.setMarketValue(new BigDecimal(getCellStringValue(row, MARKET_VALUE_COL)));

			double rawNetAsset = getCellNumericValue(row, NET_ASSET_COL);
			equityDTO.setNetAsset(BigDecimal.valueOf(rawNetAsset).multiply(BigDecimal.valueOf(100)));

			return equityDTO;
		} catch (NumberFormatException e) {
			throw new IllegalStateException("Invalid numeric value in row " + row.getRowNum(), e);
		}
	}

	private String getCellStringValue(Row row, int columnIndex) {
		if (row == null) {
			throw new IllegalArgumentException("Row cannot be null");
		}

		Cell cell = row.getCell(columnIndex);
		if (cell == null) {
			return "";
		}

		switch (cell.getCellType()) {
		case STRING:
			return cell.getStringCellValue().trim();
		case NUMERIC:
			return String.valueOf((long) cell.getNumericCellValue());
		default:
			return "";
		}
	}

	private double getCellNumericValue(Row row, int columnIndex) {
		if (row == null) {
			throw new IllegalArgumentException("Row cannot be null");
		}

		Cell cell = row.getCell(columnIndex);
		if (cell == null) {
			throw new IllegalStateException("Cell at column " + columnIndex + " is null");
		}

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
	}

	private LocalDate parsePortfolioDate(String dateText) {
		if (dateText == null || dateText.trim().isEmpty()) {
			throw new IllegalArgumentException("Date text cannot be null or empty");
		}

		String cleanDateText = DATE_PREFIX_PATTERN.matcher(dateText).replaceFirst("").trim();

		try {
			return LocalDate.parse(cleanDateText, DATE_FORMATTER);
		} catch (DateTimeParseException e) {
			throw new DateTimeParseException(
					"Unable to parse date: " + dateText + ". Expected format: 'MMMM d, yyyy' (e.g., 'June 30, 2023')",
					dateText, 0);
		}
	}

	private String determineFundType(String fundName) {
		if (fundName == null || fundName.trim().isEmpty()) {
			return "Other";
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