package com.aroha.mutualfund.factory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.aroha.mutualfund.dto.EquityDTO;
import com.aroha.mutualfund.dto.MutualFundDTO;
import com.aroha.mutualfund.exception.FileFormatException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HandlerMiraeAssetFund implements MutualFundFile {

	// Declaring all the constants
	private static final int NETASSET_COLUMN = 6;
	private static final int MARKETVALUE_COLUMN = 5;
	private static final int QUANTITY_COLUMN = 4;
	private static final int INDUSTRY_COLUMN = 3;
	private static final int ISIN_COLUMN = 2;
	private static final int INSTRUMENTNAME_COLUMN = 1;
	private static final int HEADER_ROW = 7;
	private static final int EQUITY_START_ROW = 10;
	private static final int DATEOFPORTFOLIO_COLUMN = 1;
	private static final int DATEOFPORTFOLIO_ROW = 6;
	private static final int FUNDNAME_COLUMN = 1;
	private static final int FUNDNAME_ROW = 4;

	private static final String MID_CAP = "mid cap";
	private static final String LARGE_CAP = "large cap";
	private static final String MIRAE_ASSET_MID_CAP_FUND = "mirae asset mid cap fund";
	private static final String MIRAE_ASSET_LARGE_CAP_FUND = "mirae asset large cap fund";
	private static final String SUB_TOTAL = "Sub Total";
	private static final String NET_ASSETS = "% to Net Assets";
	private static final String MARKET_FAIR_VALUE = "Market/Fair Value (Rs. in Lacs)";
	private static final String QUANTITY2 = "Quantity";
	private static final String INDUSTRY_RATING = "Industry / Rating";
	private static final String ISIN = "ISIN";
	private static final String NAME_OF_THE_INSTRUMENT = "Name of the Instrument";

	// Method to Extract data from Mira Index Mutual Fund file
	@Override
	public MutualFundDTO extractFile(Sheet sheet) {

		// To get the Fund Name
		Row row = sheet.getRow(FUNDNAME_ROW);
		Cell fundNameCell = row.getCell(FUNDNAME_COLUMN);

		// Declaring variables to store FundName, FundType, DateOfPortFolio
		String fundName = "";
		String fundType = "";
		LocalDate dateOfPortpolio = null;

		if (fundNameCell != null && fundNameCell.getCellType() == CellType.STRING
				&& !fundNameCell.getStringCellValue().trim().isEmpty()) {
			fundName = fundNameCell.getStringCellValue().trim();
		

			//Checking the FundName is matching with expected FundName
			if (MIRAE_ASSET_LARGE_CAP_FUND.equalsIgnoreCase(fundName)
					|| MIRAE_ASSET_MID_CAP_FUND.equalsIgnoreCase(fundName)) {

				//Checking FundType 
				if (fundName.toLowerCase().contains(LARGE_CAP)) {
					fundType = "Large Cap";
					
				} else if (fundName.toLowerCase().contains(MID_CAP)) {
					fundType = "Mid Cap";
					
				}
			} else {
				throw new FileFormatException("Incorrect fund name", "Mira Index Mutual Fund");
			}
		} else {
			throw new FileFormatException("Fund name not found", "Mira Index Mutual Fund");
		}

		//To get date of portfolio
		row = sheet.getRow(DATEOFPORTFOLIO_ROW);
		Cell cell = row.getCell(DATEOFPORTFOLIO_COLUMN);
		String dateofpf = "";
		if (cell != null && cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().isEmpty()) {
			dateofpf = cell.getStringCellValue();
			
			//Using Regex to find the date match
			Pattern pattern = Pattern.compile("as on (\\w+ \\d{1,2}, \\d{4})", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(dateofpf);

			//Checking whether the date found in the String
			if (matcher.find()) {
				String dateStr = matcher.group(1); // "April 30, 2025"
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
				dateOfPortpolio = LocalDate.parse(dateStr, formatter);
			} else {
				throw new FileFormatException("Date of portfolio not found", "Mira Index Mutual Fund");
			}
		} else {
			throw new FileFormatException("Date of portfolio not found", "Mira Index Mutual Fund");
		}

		//checking header names with expected column order
		row = sheet.getRow(HEADER_ROW);

		//If header Row is Empty then Throwing error
		if (row == null) {
			throw new FileFormatException("Header not found", "Mira Index Mutual Fund" + " : " + dateOfPortpolio);
		}
		
		//To get instrument name header
		cell = row.getCell(INSTRUMENTNAME_COLUMN);
		headerNullCheck(cell, dateOfPortpolio);
		String nameOfInstrument = cell.getStringCellValue();

		//To get ISIN number header
		cell = row.getCell(ISIN_COLUMN);
		headerNullCheck(cell, dateOfPortpolio);
		String isin = cell.getStringCellValue();

		//To get industry name header
		cell = row.getCell(INDUSTRY_COLUMN);
		headerNullCheck(cell, dateOfPortpolio);
		String industry = cell.getStringCellValue();

		//To get quantity header
		cell = row.getCell(QUANTITY_COLUMN);
		headerNullCheck(cell, dateOfPortpolio);
		String quantity = cell.getStringCellValue();

		//To get marketvalue header
		cell = row.getCell(MARKETVALUE_COLUMN);
		headerNullCheck(cell, dateOfPortpolio);
		String marketValue = cell.getStringCellValue();

		//To get net assets header
		cell = row.getCell(NETASSET_COLUMN);
		headerNullCheck(cell, dateOfPortpolio);
		String netAssets = cell.getStringCellValue();

		//Checking the Header name Matching to expected header names
		if (!nameOfInstrument.trim().equalsIgnoreCase(NAME_OF_THE_INSTRUMENT) || !isin.trim().equalsIgnoreCase(ISIN)
				|| !industry.trim().equalsIgnoreCase(INDUSTRY_RATING) || !quantity.trim().equalsIgnoreCase(QUANTITY2)
				|| !marketValue.replaceAll("\\s+", " ").trim().equalsIgnoreCase(MARKET_FAIR_VALUE)
				|| !netAssets.trim().equalsIgnoreCase(NET_ASSETS)) {
			
			throw new FileFormatException("Incorrect header name", "Mira Index Mutual Fund" + " : " + dateOfPortpolio);
		}

		//DTO object to store fundname, fundtype
		MutualFundDTO mutualFundDTO = new MutualFundDTO();
		List<EquityDTO> listEquity = new ArrayList<>();

		mutualFundDTO.setFundName(fundName);
		mutualFundDTO.setFundType(fundType);
		mutualFundDTO.setDateOfPortfolio(dateOfPortpolio);

		//Iterating over Equity
		for (int rowIndex = EQUITY_START_ROW; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
			
			row = sheet.getRow(rowIndex);
			if (row == null) {
				throw new FileFormatException("Values Not Found",
						"Mira Index Mutual Fund" + " : " + dateOfPortpolio);
			}

			EquityDTO equityDTO = new EquityDTO();

			cell = row.getCell(INSTRUMENTNAME_COLUMN);
			if (cell != null && cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().isEmpty()) {
				String instrumentName = cell.getStringCellValue().trim();
				if (instrumentName.equals(SUB_TOTAL)) {
					break;
				}
				equityDTO.setInstrumentName(instrumentName);
			} else {
				throw new FileFormatException(
						"Incorrect Values, at row:" + rowIndex + 1 + " column:Name of the Instrument",
						"Mira Index Mutual Fund" + " : " + dateOfPortpolio);
			}
			cell = row.getCell(ISIN_COLUMN);
			if (cell != null && cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().isEmpty()) {
				equityDTO.setIsin(cell.getStringCellValue().trim());
			} else {
				throw new FileFormatException("Incorrect Values, at row:" + rowIndex + 1 + " column:ISIN",
						"Mira Index Mutual Fund" + " : " + dateOfPortpolio);
			}

			cell = row.getCell(INDUSTRY_COLUMN);
			if (cell != null && cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().isEmpty()) {
				equityDTO.setSector(cell.getStringCellValue().trim());
			} else {
				throw new FileFormatException("Incorrect Values, at row:" + rowIndex + 1 + " column:Industry / Rating",
						"Mira Index Mutual Fund" + " : " + dateOfPortpolio);
			}

			cell = row.getCell(QUANTITY_COLUMN);
			if (cell != null && cell.getCellType() == CellType.NUMERIC) {
				equityDTO.setQuantity((int) cell.getNumericCellValue());
			} else {
				throw new FileFormatException("Incorrect Values, at row:" + rowIndex + 1 + " column:Quantity",
						"Mira Index Mutual Fund" + " : " + dateOfPortpolio);
			}

			cell = row.getCell(MARKETVALUE_COLUMN);
			if (cell != null && cell.getCellType() == CellType.NUMERIC) {
				double numericValue = cell.getNumericCellValue();
				BigDecimal marketVal = BigDecimal.valueOf(numericValue);
				equityDTO.setMarketValue(marketVal);
			} else {
				throw new FileFormatException(
						"Incorrect Values, at row:" + rowIndex + 1 + " column:Market/Fair Value (Rs. in Lacs)",
						"Mira Index Mutual Fund" + " : " + dateOfPortpolio);
			}

			cell = row.getCell(NETASSET_COLUMN);
			if (cell != null && cell.getCellType() == CellType.NUMERIC) {
				double rawValue = cell.getNumericCellValue(); // will be 0.0007
				BigDecimal percentage = BigDecimal.valueOf(rawValue).multiply(BigDecimal.valueOf(100));
				equityDTO.setNetAsset(percentage); // set 0.07
			} else {
				throw new FileFormatException("Incorrect Values, at row:" + rowIndex + 1 + " column:% to Net Assets",
						"Mira Index Mutual Fund" + " : " + dateOfPortpolio);
			}

			listEquity.add(equityDTO);
		}
		mutualFundDTO.setEquity(listEquity);
		return mutualFundDTO;
	}

	public void headerNullCheck(Cell cell, LocalDate dateOfPortpolio) {
		if (cell == null) {
			throw new FileFormatException("Header not found", "Mira Index Mutual Fund" + " : " + dateOfPortpolio);
		}
	}

}
