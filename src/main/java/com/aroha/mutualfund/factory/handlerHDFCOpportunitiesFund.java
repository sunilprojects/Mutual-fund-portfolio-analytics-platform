package com.aroha.mutualfund.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.aroha.mutualfund.pojo.RowEntityBundle;

public class handlerHDFCOpportunitiesFund implements MutualFundFile {

	@Override
	public List<RowEntityBundle> extractFile(Sheet sheet) {
		
//		//TODO Sunil
//		// TODO Auto-generated method stub
//		Map<String, Integer> columnIndexMap = new HashMap<>();
//        boolean headerMapped = false;
//
//        for (Row row : sheet) {
//            int rowNum = row.getRowNum();
//
//            // Step 1: Map header columns
//            if (!headerMapped) {
//                for (Cell cell : row) {
//                    if (cell.getCellType() == CellType.STRING) {
//                        String header = cell.getStringCellValue().trim();
//
//                        if (header.equalsIgnoreCase("ISIN") ||
//                            header.equalsIgnoreCase("Coupon (%)") ||
//                            header.equalsIgnoreCase("Name Of the Instrument") ||
//                            header.equalsIgnoreCase("Industry /Rating") ||
//                            header.equalsIgnoreCase("Quantity")) {
//                            columnIndexMap.put(header, cell.getColumnIndex());
//                        }
//                    }
//                }
//                for(Map.Entry<String, Integer> m1:columnIndexMap.entrySet()) {
//                	System.out.println(m1.getKey()+m1.getValue());
//	            	
//	            }
//
//                // Proceed only if all required headers are found
//                if (columnIndexMap.size() == 5) {
//                    headerMapped = true;
//                }
//                continue;
//                
//            }
//            
//          
//
//            // Step 2: Extract only required fields
//            String isin = getCellValue(row.getCell(columnIndexMap.get("ISIN")));
//            String coupon = getCellValue(row.getCell(columnIndexMap.get("Coupon (%)")));
//            String name = getCellValue(row.getCell(columnIndexMap.get("Name Of the Instrument")));
//            String industry = getCellValue(row.getCell(columnIndexMap.get("Industry /Rating")));
//            String quantity = getCellValue(row.getCell(columnIndexMap.get("Quantity")));
//
//            if (!isin.isEmpty()) {
//                System.out.println("ISIN: " + isin +
//                                   ", Coupon: " + coupon +
//                                   ", Name: " + name +
//                                   ", Industry: " + industry +
//                                   ", Quantity: " + quantity);
//            }
//          
//        }
//        
//
//        processedFiles.add(filename);
    
		return null;
	}

}
