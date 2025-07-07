package com.aroha.mutualfund.factory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.aroha.mutualfund.dto.EquityDTO;
import com.aroha.mutualfund.dto.MutualFundDTO;
import com.aroha.mutualfund.exception.FileFormatException;
import com.aroha.mutualfund.exception.FundValidationException;
import com.aroha.mutualfund.exception.PortfolioDateParseException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HandlerDSPFund implements MutualFundFile {

	private static final Pattern DATE_PREFIX_PATTERN = Pattern.compile(".*[Aa]s [Oo]n");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");
	private static final int DATA_START_ROW = 7;
	private static final int INSTRUMENT_NAME_COL = 1;
	private static final int ISIN_CODE_COL = 2;
	private static final int SECTOR_COL = 3;
	private static final int QUANTITY_COL = 4;
	private static final int MARKET_VALUE_COL = 5;
	private static final int NET_ASSET_COL = 6;

	@Override
	public MutualFundDTO extractFile(Sheet sheet) {
		log.info("==== DSP file processing started ====");
		if (sheet == null) {
			log.error("Provided sheet is null");
			throw new FileFormatException("Invalid file format", "DSP Mid cap Fund");
		}

		MutualFundDTO mutualFundDTO = new MutualFundDTO();
		List<EquityDTO> equityList = new ArrayList<>();
		extractFundInfo(sheet, mutualFundDTO);
		processHoldingsData(sheet, equityList);
		mutualFundDTO.setEquity(equityList);
		log.info("==== DSP file Processed successfully ==== ");
		return mutualFundDTO;
	}
	//Extracting fund information and setting to mutual fund dto
	private void extractFundInfo(Sheet sheet, MutualFundDTO mutualFundDTO) {
		log.info("Fund extraction started");
		String fundName = getCellStringValue(sheet.getRow(0), 1);
		if (fundName == null || fundName.trim().isEmpty()) {
			log.error("fund name is empty !!!!");
			throw new FundValidationException("Fund name is empty", "DSP Mutual Fund Allocation");
		}

		String dateText = getCellStringValue(sheet.getRow(1), 1);
		LocalDate portfolioDate = parsePortfolioDate(dateText);

		mutualFundDTO.setFundName(fundName);
		mutualFundDTO.setFundType(determineFundType(fundName));
		mutualFundDTO.setDateOfPortfolio(portfolioDate);
		log.info("Fund extraction ends", mutualFundDTO.getFundName(), mutualFundDTO.getFundType(),
				mutualFundDTO.getDateOfPortfolio());
	}
	//Extracting equities and setting to equity dto
	private void processHoldingsData(Sheet sheet, List<EquityDTO> equityList) {
		log.debug("Starting to process holdings data from row {}", DATA_START_ROW);
		for (int rowNum = DATA_START_ROW; rowNum <= sheet.getLastRowNum(); rowNum++) {
			Row row = sheet.getRow(rowNum);
			if (row == null) {
				log.debug("Skipping null row at index : ", rowNum);
				continue;
			}
			//terminating loop after last equity
			if (isLastEquity(row)) {
				log.debug("Reached end of equity data at row : ", rowNum);
				break;

			}

			EquityDTO equityDTO = createEquityDTO(row);
			if (equityDTO != null) {
				// adding equity to list
				equityList.add(equityDTO);
			}

		}
	}

	private boolean isLastEquity(Row row) {
		Cell cell = row.getCell(INSTRUMENT_NAME_COL);
		return cell != null && "Total".equalsIgnoreCase(cell.getStringCellValue().trim());
	}
	//Validating and setting equities to equity dto
	private EquityDTO createEquityDTO(Row row) {
		String instrumentName = getCellStringValue(row, INSTRUMENT_NAME_COL);
		String isinCode = getCellStringValue(row, ISIN_CODE_COL);
		String sector = getCellStringValue(row, SECTOR_COL);

		if (instrumentName == null || instrumentName.trim().isEmpty()) {
			log.error("Missing instrument name in row " + (row.getRowNum() + 1), "DSP Mutual Fund Allocation");
			throw new FundValidationException("Missing instrument name in row " + (row.getRowNum() + 1),
					"DSP Mutual Fund Allocation");
		}
		if (isinCode == null || isinCode.trim().isEmpty()) {
			log.error("Missing ISIN code in row " + (row.getRowNum() + 1), "DSP Mutual Fund Allocation");
			throw new FundValidationException("Missing ISIN code in row " + (row.getRowNum() + 1),
					"DSP Mutual Fund Allocation");
		}
		if (sector == null || sector.trim().isEmpty()) {
			log.error("Missing sector in row " + (row.getRowNum() + 1), "DSP Mutual Fund Allocation");
			throw new FundValidationException("Missing sector in row " + (row.getRowNum() + 1),
					"DSP Mutual Fund Allocation");
		}

		int quantity = (int) getCellNumericValue(row, QUANTITY_COL);
		BigDecimal marketValue = new BigDecimal(getCellNumericValue(row, MARKET_VALUE_COL));
		BigDecimal netAsset = BigDecimal.valueOf(getCellNumericValue(row, NET_ASSET_COL))
				.multiply(BigDecimal.valueOf(100));

		EquityDTO equityDTO = new EquityDTO();
		equityDTO.setInstrumentName(instrumentName);
		equityDTO.setIsin(isinCode);
		equityDTO.setSector(sector);
		equityDTO.setQuantity(quantity);
		equityDTO.setMarketValue(marketValue);
		equityDTO.setNetAsset(netAsset);
		return equityDTO;
	}

	private String getCellStringValue(Row row, int columnIndex) {
		if (row == null)
			return "";

		Cell cell = row.getCell(columnIndex);
		if (cell == null)
			return "";

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
		Cell cell = row.getCell(columnIndex);
		if (cell == null) {
			log.error("Missing numeric cell at column " + columnIndex, "DSP Mutual Fund Allocation");
			throw new FundValidationException("Missing numeric cell at column " + columnIndex,
					"DSP Mutual Fund Allocation");
		}

		switch (cell.getCellType()) {
		case NUMERIC:
			return cell.getNumericCellValue();
		case STRING:
			String value = cell.getStringCellValue().trim();
			if (value.isEmpty())
				throw new FundValidationException("Empty numeric value", "DSP Mutual Fund Allocation");
			return Double.parseDouble(value);
		default:
			throw new FundValidationException("Unsupported cell type at column " + columnIndex,
					"DSP Mutual Fund Allocation");
		}
	}

	//Parsing date
	private LocalDate parsePortfolioDate(String dateText) {
		if (dateText == null || dateText.trim().isEmpty()) {
			log.error("Date text is null or empty !!!!");
			throw new FundValidationException("Portfolio date is missing or empty", "DSP Mutual Fund Allocation");
		}

		try {
			String cleanDateText = DATE_PREFIX_PATTERN.matcher(dateText).replaceFirst("").trim();
			return LocalDate.parse(cleanDateText, DATE_FORMATTER);

		} catch (DateTimeParseException e) {
			log.error("Failed to parse date !!!! ", dateText, " " + e);
			throw new PortfolioDateParseException(
					"Invalid date format: '" + dateText + "'. Expected format: 'MMMM d, yyyy'",
					"DSP Mutual Fund Allocation");
		}

	}

	//determining fund type based on fund name
	private String determineFundType(String fundName) {
		if (fundName == null || fundName.trim().isEmpty())
			return "Other";

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