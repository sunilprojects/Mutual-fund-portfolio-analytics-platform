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

import org.apache.commons.logging.Log;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;

import com.aroha.mutualfund.dto.EquityDTO;
import com.aroha.mutualfund.dto.MutualFundDTO;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class handlerHDFCOpportunitiesFund implements MutualFundFile {

	private String getSafeValue(Row row, Integer colIndex) {
		if (colIndex == null) return "";
		Cell cell = row.getCell(colIndex);
		if (cell == null) return "";

		switch (cell.getCellType()) {
		case STRING:
			return cell.getStringCellValue().trim();
		case NUMERIC:
			return String.valueOf(cell.getNumericCellValue());
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		case FORMULA:
			return cell.getCellFormula();
		default:
			return "";
		}
	}

	@Override
	public MutualFundDTO extractFile(Sheet sheet) {
		Map<String, Integer> columnIndexMap = new HashMap<>();
		boolean headerMapped = false;
		List<EquityDTO> equityList = new ArrayList<>();
		String fundName = "";
		String fundType = "";
		LocalDate dateOfPortfolio=null;


		Row firstRow = sheet.getRow(0);
		if (firstRow != null) {
			System.out.print(" First Row Header: ");
			for (Cell cell : firstRow) {
				String value = getSafeValue(firstRow, cell.getColumnIndex());
				//  Extract fund name
				if (value.contains("(")) {
					fundName = value.substring(0, value.indexOf("(")).trim();
				} else {
					fundName = value.trim();
				}

				// Extract fund type from inside brackets
				if (value.toLowerCase().contains("mid cap")) {
					fundType = "mid cap";
				} else if (value.toLowerCase().contains("small cap")) {
					fundType = "small cap";
				} else if (value.toLowerCase().contains("large cap")) {
					fundType = "large cap";
				}
				break; // only first cell  

			}
		}

		Row secondRow = sheet.getRow(1);
		if (secondRow != null) {
			for (Cell cell : secondRow) {
				String value = getSafeValue(secondRow, cell.getColumnIndex());
				System.out.println("📄 Second Row: " + value);

				// Try to extract date using regex
				Pattern datePattern = Pattern.compile("(\\d{1,2})-(\\w+)-(\\d{4})"); // e.g., 30-Apr-2025
				Matcher matcher = datePattern.matcher(value);

				if (matcher.find()) {
					String day = matcher.group(1);
					String monthStr = matcher.group(2);
					String year = matcher.group(3);

					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MMM-yyyy", Locale.ENGLISH);
					dateOfPortfolio = LocalDate.parse(day + "-" + monthStr + "-" + year, formatter);
				}
				break; // only first cell
			}
		}


		for (Row row : sheet) {
			if (row == null || row.getPhysicalNumberOfCells() == 0) continue;

			if (!headerMapped) {
				for (Cell cell : row) {
					if (cell.getCellType() == CellType.STRING) {
						String header = cell.getStringCellValue().trim();
						if (header.equalsIgnoreCase("ISIN") ||
								header.equalsIgnoreCase("Name Of the Instrument") ||
								header.equalsIgnoreCase("Industry+ /Rating") ||
								header.equalsIgnoreCase("Quantity") ||
								header.equalsIgnoreCase("Market/ Fair Value (Rs. in Lacs.)")||
								header.equalsIgnoreCase("% to NAV")) {
							columnIndexMap.put(header, cell.getColumnIndex());
						}
					}
				}

				if (columnIndexMap.size() == 6) {
					headerMapped = true;
				}
				continue;
			}

			// Step 2: Extract values (excluding coupon)
			String isin = getSafeValue(row, columnIndexMap.get("ISIN"));
			String name = getSafeValue(row, columnIndexMap.get("Name Of the Instrument"));
			String industry = getSafeValue(row, columnIndexMap.get("Industry+ /Rating"));
			String quantityStr = getSafeValue(row, columnIndexMap.get("Quantity"));
			String marketValueStr = getSafeValue(row, columnIndexMap.get("Market/ Fair Value (Rs. in Lacs.)"));
			String netAssetStr = getSafeValue(row, columnIndexMap.get("% to NAV"));


			// Step 3: Only print row if all required fields are present
			if (isin.isEmpty() || name.isEmpty() || industry.isEmpty() || quantityStr.isEmpty() || marketValueStr.isEmpty()||netAssetStr.isEmpty()) {
				continue;
			}


			try {
				int quantity = Integer.parseInt(quantityStr.replace(",", "").split("\\.")[0]);
				BigDecimal marketValue = new BigDecimal(marketValueStr.replace(",", ""));
				BigDecimal netAsset = new BigDecimal(netAssetStr.replace(",", ""));

				EquityDTO equityDTO = EquityDTO.builder()
						.isin(isin)
						.instrumentName(name)
						.sector(industry)
						.quantity(quantity)
						.marketValue(marketValue)
						.netAsset(netAsset)
						.build();

				equityList.add(equityDTO);

			} catch (Exception e) {
				System.out.println("Skipping row due to parsing error: " + e.getMessage());
			}

		}
		return MutualFundDTO.builder()
				.fundName(fundName)
				.fundType(fundType)
				.dateOfPortfolio(dateOfPortfolio)
				.createdBy(null)         // or set default like "system"
				.updatedBy(null)
				.equity(equityList)
				.build();

	}

}
