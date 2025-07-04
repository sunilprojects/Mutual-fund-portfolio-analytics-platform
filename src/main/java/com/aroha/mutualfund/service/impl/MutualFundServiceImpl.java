
package com.aroha.mutualfund.service.impl;

import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aroha.mutualfund.dto.EquityDTO;
import com.aroha.mutualfund.dto.FundsResponceDTO;

import com.aroha.mutualfund.dto.HoldingDetail;

import com.aroha.mutualfund.dto.MutualFundDTO;
import com.aroha.mutualfund.exception.ExcelProcessingException;
import com.aroha.mutualfund.factory.FilesFactory;
import com.aroha.mutualfund.factory.MutualFundFile;
import com.aroha.mutualfund.repository.FundRepository;
import com.aroha.mutualfund.repository.HoldingTransactionsRepository;
import com.aroha.mutualfund.repository.HoldingsRepository;
import com.aroha.mutualfund.repository.InstrumentRepository;
import com.aroha.mutualfund.service.MutualFundService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MutualFundServiceImpl implements MutualFundService {

	private FundRepository fundRepository;
	private InstrumentRepository instrumentRepository;
	private HoldingsRepository holdingsRepository;
	private HoldingTransactionsRepository holdingTransactionsRepository;

	public MutualFundServiceImpl(FundRepository mutualFundRepository, InstrumentRepository instrumentRepository,
			HoldingsRepository holdingsRepository, HoldingTransactionsRepository holdingTransactionsRepository) {
		this.fundRepository = mutualFundRepository;
		this.instrumentRepository = instrumentRepository;
		this.holdingsRepository = holdingsRepository;
		this.holdingTransactionsRepository = holdingTransactionsRepository;
	}

	@Override
	public ResponseEntity<String> processFundFile(MultipartFile[] files,String userName) {
		if (files == null || files.length == 0) {
			return ResponseEntity.ok("No Files uploaded..");
		}

		for (MultipartFile file : files) {
			String filename = file.getOriginalFilename();

			if (filename == null
					|| (!filename.toLowerCase().endsWith(".xls") && !filename.toLowerCase().endsWith(".xlsx"))) {
				continue;
			}

			try (InputStream inputStream = file.getInputStream();
					Workbook workbook = filename.toLowerCase().endsWith(".xlsx") ? new XSSFWorkbook(inputStream)
							: new HSSFWorkbook(inputStream)) {

				Sheet sheet = workbook.getSheetAt(0);

				if (filename.contains(" - ")) {
					filename = filename.substring(0, filename.indexOf(" - ")).trim();
				}
				System.out.println("updated filename:" + filename);

				FilesFactory filesFactory = new FilesFactory();
				MutualFundFile mutualFundFile = filesFactory.getFile(filename);

				MutualFundDTO fieldList = mutualFundFile.extractFile(sheet);

				log.info("{}", fieldList.getEquity().size());
				
				if(mutualFundFile==null) {
					continue;
				}
				// TODO: handle null
				// TODO: pass Sheet

				MutualFundDTO mutualFundDTO = mutualFundFile.extractFile(sheet);
				processedFiles.add(filename);
				log.info("{}", mutualFundDTO.getEquity().size());

				//Save file to folder
				saveFileToFolder(file, "uploaded-files/success");


				int fundid = fundRepository.insertFundIfNotExists(mutualFundDTO.getFundName(),
						mutualFundDTO.getFundType(), userName);

				if (mutualFundDTO.getEquity() != null) {
					for (EquityDTO equity : mutualFundDTO.getEquity()) {
						//log.info("MarketValue:{}", equity.getMarketValue());
						int instrumentId = instrumentRepository.insertInstrumentIfNotExists(equity.getIsin(),
								equity.getInstrumentName(), equity.getSector(), userName);
						int holdingId = holdingsRepository.insertHoldingIfNotExists(fundid, instrumentId, userName);
						holdingTransactionsRepository.upsertTransaction(holdingId, mutualFundDTO.getDateOfPortfolio(),
								equity.getQuantity(), equity.getMarketValue(), equity.getNetAsset(), userName);
					}
				}

			} catch (Exception e) {
				log.error("Failed to open or parse Excel file: {}", filename, e);
				throw new  ExcelProcessingException("Failed to process Excel file: " , filename);
			}

		}
		return ResponseEntity.ok("All Files processed succesfully..");
		return "Processed Files: " + processedFiles + "\nSkipped Files:" + skippedFiles+  " !!!!!";
	}

	@Override
	public List<FundsResponceDTO> getAllFunds() {
		return fundRepository.getAllFunds();
	}
	@Override
	public List<String> getSectorsByFundId(int fundId) {
		return instrumentRepository.findSectorsByFundId(fundId);
	}

	public List<HoldingDetail> getFundHoldings(int fundId) {
		return holdingsRepository.getHoldingsByFundId(fundId);
	}

	private void saveFileToFolder(MultipartFile file, String folderPath) throws IOException {
		File dir = new File(folderPath);
		if (!dir.exists()) {
			dir.mkdirs(); // create the folder if it doesn't exist
		}
		 // Construct the full file path where the file will be saved (folderPath/filename)
		String filePath = folderPath + File.separator + file.getOriginalFilename();
		// Use try-with-resources to automatically close streams after use
		try (InputStream in = file.getInputStream();// InputStream from the uploaded MultipartFile
				OutputStream out = new FileOutputStream(filePath)) { // OutputStream to the destination file

			byte[] buffer = new byte[1024];// Create a 1 KB buffer for efficient reading/writing
			int length;
			// Read from the input stream into the buffer and write to the output stream
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length); // Write only the number of bytes actually read
			}
		}
	}


}
