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


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class handlerMiraeAssetFund implements MutualFundFile {

	@Override
	public MutualFundDTO extractFile(Sheet sheet) {

		// TODO Rajith
		// TODO Auto-generated method stub

		Row row = sheet.getRow(4);
		Cell fundNameCell = row.getCell(1);

		String fundName = "";
		String fundType = "";
		LocalDate dateOfPortpolio = null;

		if (fundNameCell != null && fundNameCell.getCellType() == CellType.STRING) {
			fundName = fundNameCell.getStringCellValue().trim();
			log.info(fundName);

			if ("mirae asset large cap fund".equalsIgnoreCase(fundName)
					|| "mirae asset mid cap fund".equalsIgnoreCase(fundName)) {

				if (fundName.toLowerCase().contains("large cap")) {
					fundType = "Large Cap";
					log.info("Large Cap");
				} else if (fundName.toLowerCase().contains("mid cap")) {
					fundType = "Mid Cap";
					log.info("Mid Cap");
				}
			}
		}

		// To get date of portpolio
		String input = sheet.getRow(6).getCell(1).getStringCellValue();
		log.info(input);
		Pattern pattern = Pattern.compile("as on (\\w+ \\d{1,2}, \\d{4})", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(input);

		if (matcher.find()) {
			String dateStr = matcher.group(1); // "April 30, 2025"
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
			dateOfPortpolio = LocalDate.parse(dateStr, formatter);
			log.info("{}", dateOfPortpolio);

		}

		// checking header names with expected column order
		row = sheet.getRow(7);
		String nameOfInstrument = row.getCell(1).getStringCellValue();
		String isin = row.getCell(2).getStringCellValue();
		String industry = row.getCell(3).getStringCellValue();
		String quantity = row.getCell(4).getStringCellValue();
		String marketValue = row.getCell(5).getStringCellValue();
		String netAssets = row.getCell(6).getStringCellValue();

		if (!nameOfInstrument.trim().equalsIgnoreCase("Name of the Instrument") || !isin.trim().equalsIgnoreCase("ISIN")
				|| !industry.trim().equalsIgnoreCase("Industry / Rating")
				|| !quantity.trim().equalsIgnoreCase("Quantity")
				|| !marketValue.replaceAll("\\s+", " ").trim().equalsIgnoreCase("Market/Fair Value (Rs. in Lacs)")
				|| !netAssets.trim().equalsIgnoreCase("% to Net Assets")) {
			log.info("File Format Not Proper");
		}

		MutualFundDTO mutualFundDTO=new MutualFundDTO();
		List<EquityDTO> listEquity=new ArrayList<>();
		
		mutualFundDTO.setFundName(fundName);
		mutualFundDTO.setFundType(fundType);
		mutualFundDTO.setDateOfPortfolio(dateOfPortpolio);
		 
		// Iterate over Equity
		for (int rowIndex = 10; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
			row = sheet.getRow(rowIndex);
			if (row == null)
				continue;
			
			EquityDTO equityDTO=new EquityDTO();
			
			Cell cell = row.getCell(1);
			if (cell != null) {

				String instrumentName = cell.getStringCellValue();
				if (instrumentName.trim().equals("Sub Total")) {
					break;
				}
				equityDTO.setInstrumentName(instrumentName.trim());
			}
			cell = row.getCell(2);
			if (cell != null && cell.getCellType() == CellType.STRING) {
				equityDTO.setIsin(cell.getStringCellValue().trim());
			}
			cell = row.getCell(3);
			if (cell != null && cell.getCellType() == CellType.STRING) {
				equityDTO.setSector(cell.getStringCellValue().trim());
			}
			cell = row.getCell(4);
			if (cell != null && cell.getCellType() == CellType.NUMERIC) {
				equityDTO.setQuantity((int)cell.getNumericCellValue());
			}
			
			cell = row.getCell(5);
			if (cell != null && cell.getCellType() == CellType.NUMERIC) {
			    double numericValue = cell.getNumericCellValue();
			    BigDecimal marketVal = BigDecimal.valueOf(numericValue);
			    equityDTO.setMarketValue(marketVal);
			}
			
			cell = row.getCell(6);
			if (cell != null && cell.getCellType() == CellType.NUMERIC) {
				double rawValue = cell.getNumericCellValue(); // will be 0.0007
			    BigDecimal percentage = BigDecimal.valueOf(rawValue).multiply(BigDecimal.valueOf(100));
			    equityDTO.setNetAsset(percentage); // set 0.07
			}
			
			listEquity.add(equityDTO);
		}
		mutualFundDTO.setEquity(listEquity);
		return mutualFundDTO;
	}

}
