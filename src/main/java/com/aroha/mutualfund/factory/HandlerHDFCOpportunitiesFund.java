package com.aroha.mutualfund.factory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
public class HandlerHDFCOpportunitiesFund implements MutualFundFile {

	private String getSafeValue(Row row, Integer colIndex) {
		// If column index is null, return empty
		if (colIndex == null)
			return "";
		// Get the cell at the given column index
		Cell cell = row.getCell(colIndex);
		if (cell == null)
			return "";
		// Determine the type of the cell and return appropriate value
		switch (cell.getCellType()) {
		case STRING:
			// Return trimmed string value
			return cell.getStringCellValue().trim();
		case NUMERIC:
			// Return numeric value as string
			return String.valueOf(cell.getNumericCellValue());
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		case FORMULA:
			// Return the formula as a string
			return cell.getCellFormula();
		default:
			// Return empty string for unsupported types
			return "";
		}
	}

	@Override
	public MutualFundDTO extractFile(Sheet sheet) {
		Map<String, Integer> columnIndexMap = new HashMap<>();
		boolean headerMapped = false;
		boolean startValidation = false;
		// To collect parsed equity rows
		List<EquityDTO> equityList = new ArrayList<>();
		// Fund data extracting
		String fundName = "";
		String fundType = "";
		LocalDate dateOfPortfolio = null;

		boolean fundInfoFound = false;
		boolean dateFound = false;

		log.info("Starting to scan sheet for fund name and portfolio date...");

		// Extract fund name and portfolio date from top of sheet
		outerLoop: for (Row row : sheet) {
			for (Cell cell : row) {
				String value = getSafeValue(row, cell.getColumnIndex());
				if (value.contains("HDFC Mid-Cap Opportunities Fund")) {
					fundName = value.substring(0, value.indexOf("(")).trim();
					String lower = value.toLowerCase();
					if (lower.contains("mid cap"))
						fundType = "mid cap";
					else if (lower.contains("small cap"))
						fundType = "small cap";
					else if (lower.contains("large cap"))
						fundType = "large cap";
					else
						fundType = "other";
					fundInfoFound = true;
				}
				if (value.toLowerCase().contains("portfolio as on")) {
					Pattern pattern = Pattern.compile("(\\d{1,2})-(\\w+)-(\\d{4})");
					Matcher matcher = pattern.matcher(value);
					if (matcher.find()) {
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MMM-yyyy", Locale.ENGLISH);
						dateOfPortfolio = LocalDate
								.parse(matcher.group(1) + "-" + matcher.group(2) + "-" + matcher.group(3), formatter);
						dateFound = true;
					}
				}
				// Break if both found
				if (fundInfoFound && dateFound) {
					break outerLoop;
				}
			}
		}
		// validations for missing fundName
		if (fundName.isEmpty()) {
			log.error("Fund name not found in sheet.");
			throw new FileFormatException("Fund name not found in the sheet.", fundName + "-" + dateOfPortfolio);
		}
		// validations for missing dateOfPortfolio
		if (dateOfPortfolio == null) {
			log.error("Portfolio date not found or invalid.");
			throw new FileFormatException("Portfolio date not found or invalid format.",
					fundName + "-" + dateOfPortfolio);
		}

		// for fields
		for (Row row : sheet) {
			if (row == null)
				continue;

			// Header mapping
			if (!headerMapped) {
				for (Cell cell : row) {
					String header = getSafeValue(row, cell.getColumnIndex());
					if (header.equalsIgnoreCase("ISIN"))
						columnIndexMap.put("ISIN", cell.getColumnIndex());
					else if (header.equalsIgnoreCase("Name Of the Instrument"))
						columnIndexMap.put("Name", cell.getColumnIndex());
					else if (header.equalsIgnoreCase("Industry+ /Rating"))
						columnIndexMap.put("Industry", cell.getColumnIndex());
					else if (header.equalsIgnoreCase("Quantity"))
						columnIndexMap.put("Quantity", cell.getColumnIndex());
					else if (header.equalsIgnoreCase("Market/ Fair Value (Rs. in Lacs.)"))
						columnIndexMap.put("MarketValue", cell.getColumnIndex());
					else if (header.equalsIgnoreCase("% to NAV"))
						columnIndexMap.put("NetAsset", cell.getColumnIndex());
				}
				// If all 6 columns found, mapping is complete
				if (columnIndexMap.size() == 6)
					headerMapped = true;
				continue;
			}

			// Extract values
			String isin = getSafeValue(row, columnIndexMap.get("ISIN"));
			String name = getSafeValue(row, columnIndexMap.get("Name"));
			String industry = getSafeValue(row, columnIndexMap.get("Industry"));
			String quantityStr = getSafeValue(row, columnIndexMap.get("Quantity"));
			String marketValueStr = getSafeValue(row, columnIndexMap.get("MarketValue"));
			String netAssetStr = getSafeValue(row, columnIndexMap.get("NetAsset"));

			// Step 3: Count non-empty required fields
			int nonEmptyCount = 0;
			if (!isin.isEmpty())
				nonEmptyCount++;
			if (!name.isEmpty())
				nonEmptyCount++;
			if (!industry.isEmpty())
				nonEmptyCount++;
			if (!quantityStr.isEmpty())
				nonEmptyCount++;
			if (!marketValueStr.isEmpty())
				nonEmptyCount++;
			if (!netAssetStr.isEmpty())
				nonEmptyCount++;

			// Skipping pre-data footer rows like row 6, 7, 88
			if (nonEmptyCount < 6) {
				if (!startValidation) {
					log.warn(" Skipping row {} due to missing fields.", row.getRowNum() + 1);
					continue; // Before real data begins
				} else {
					continue; // After data start, still allow skipping blank rows
				}
			}

			// First valid data row found
			if (!startValidation)
				startValidation = true;

			// parsing fileds
			try {
				int quantity = Integer.parseInt(quantityStr.replace(",", "").split("\\.")[0]);
				BigDecimal marketValue = new BigDecimal(marketValueStr.replace(",", ""));
				BigDecimal netAsset = new BigDecimal(netAssetStr.replace(",", ""));
				// Build EquityDTO and add to list
				EquityDTO equityDTO = EquityDTO.builder().isin(isin).instrumentName(name).sector(industry)
						.quantity(quantity).marketValue(marketValue).netAsset(netAsset).build();

				equityList.add(equityDTO);

			} catch (Exception e) {
				log.error(" Error parsing row {}: {}", row.getRowNum() + 1, e.getMessage());
				throw new FileFormatException("Error parsing row " + (row.getRowNum() + 1) + ": ",
						fundName + "-" + dateOfPortfolio);
			}
		}
		// Build and return the final MutualFundDTO
		return MutualFundDTO.builder().fundName(fundName).fundType(fundType).dateOfPortfolio(dateOfPortfolio)
				.equity(equityList).createdBy(null).updatedBy(null).build();
	}
}
