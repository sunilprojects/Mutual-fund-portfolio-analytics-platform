package com.aroha.mutualfund.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aroha.mutualfund.service.MutualFundService;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class MutualFundServiceImpl implements MutualFundService {
	@Override
	public String processFundFile(MultipartFile[] files) {
	    if (files == null || files.length == 0) {
	        return "No files uploaded";
	    }

	    List<String> processedFiles = new ArrayList<>();
	    List<String> skippedFiles = new ArrayList<>();

	    for (MultipartFile file : files) {
	        String filename = file.getOriginalFilename();

	        if (filename == null || 
	           (!filename.toLowerCase().endsWith(".xls") && !filename.toLowerCase().endsWith(".xlsx"))) {
	            skippedFiles.add(filename);
	            continue;
	        }

	        try (InputStream inputStream = file.getInputStream();
	             Workbook workbook = filename.toLowerCase().endsWith(".xlsx") ?
	                                 new XSSFWorkbook(inputStream) :
	                                 new HSSFWorkbook(inputStream)) {

	            Sheet sheet = workbook.getSheetAt(0);
	            Map<String, Integer> columnIndexMap = new HashMap<>();
	            boolean headerMapped = false;

	            for (Row row : sheet) {
	                int rowNum = row.getRowNum();

	                // Step 1: Map header columns
	                if (!headerMapped) {
	                    for (Cell cell : row) {
	                        if (cell.getCellType() == CellType.STRING) {
	                            String header = cell.getStringCellValue().trim();

	                            if (header.equalsIgnoreCase("ISIN") ||
	                                header.equalsIgnoreCase("Coupon (%)") ||
	                                header.equalsIgnoreCase("Name Of the Instrument") ||
	                                header.equalsIgnoreCase("Industry /Rating") ||
	                                header.equalsIgnoreCase("Quantity")) {
	                                columnIndexMap.put(header, cell.getColumnIndex());
	                            }
	                        }
	                    }
	                    for(Map.Entry<String, Integer> m1:columnIndexMap.entrySet()) {
		                	System.out.println(m1.getKey()+m1.getValue());
			            	
			            }

	                    // Proceed only if all required headers are found
	                    if (columnIndexMap.size() == 5) {
	                        headerMapped = true;
	                    }
	                    continue;
	                    
	                }
	                
	              

	                // Step 2: Extract only required fields
	                String isin = getCellValue(row.getCell(columnIndexMap.get("ISIN")));
	                String coupon = getCellValue(row.getCell(columnIndexMap.get("Coupon (%)")));
	                String name = getCellValue(row.getCell(columnIndexMap.get("Name Of the Instrument")));
	                String industry = getCellValue(row.getCell(columnIndexMap.get("Industry /Rating")));
	                String quantity = getCellValue(row.getCell(columnIndexMap.get("Quantity")));

	                if (!isin.isEmpty()) {
	                    System.out.println("ISIN: " + isin +
	                                       ", Coupon: " + coupon +
	                                       ", Name: " + name +
	                                       ", Industry: " + industry +
	                                       ", Quantity: " + quantity);
	                }
	              
	            }
	            

	            processedFiles.add(filename);
	            
 
	        } catch (Exception e) {
	            skippedFiles.add(filename);
	            e.printStackTrace();
	        }
	    }

	    return "Processed Files: " + processedFiles + "\nSkipped Files: " + skippedFiles;
	}

	// 🔧 Utility method to get cell value as string
	private String getCellValue(Cell cell) {
	    if (cell == null) return "";
	    switch (cell.getCellType()) {
	        case STRING:
	            return cell.getStringCellValue().trim();
	        case NUMERIC:
	            // Check if it's a decimal (e.g., Coupon %), then return as string
	            double value = cell.getNumericCellValue();
	            if (value == Math.floor(value)) {
	                return String.valueOf((long) value); // no decimal
	            } else {
	                return String.valueOf(value); // keep decimal
	            }
	        case BOOLEAN:
	            return String.valueOf(cell.getBooleanCellValue());
	        case FORMULA:
	            return cell.getCellFormula();
	        default:
	            return "";
	    }
	}


   }

