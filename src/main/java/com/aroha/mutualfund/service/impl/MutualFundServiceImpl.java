
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
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aroha.mutualfund.dto.EquityDTO;
import com.aroha.mutualfund.dto.FundsResponceDTO;
import com.aroha.mutualfund.dto.HoldingDetail;
import com.aroha.mutualfund.dto.MutualFundDTO;
import com.aroha.mutualfund.exception.ExcelProcessingException;
import com.aroha.mutualfund.exception.DBOperationFailureException;
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

	// Constructor injection
	public MutualFundServiceImpl(FundRepository mutualFundRepository, InstrumentRepository instrumentRepository,
			HoldingsRepository holdingsRepository, HoldingTransactionsRepository holdingTransactionsRepository) {
		this.fundRepository = mutualFundRepository;
		this.instrumentRepository = instrumentRepository;
		this.holdingsRepository = holdingsRepository;
		this.holdingTransactionsRepository = holdingTransactionsRepository;
	}

	// To process Uploaded Files
	@Override
	public ResponseEntity<String> processFundFile(MultipartFile[] files, String userName) {
		if (files == null || files.length == 0) {
			return ResponseEntity.ok("No Files uploaded..");
		}

		for (MultipartFile file : files) {
			String filename = file.getOriginalFilename();

			if (filename == null
					|| (!filename.toLowerCase().endsWith(".xls") && !filename.toLowerCase().endsWith(".xlsx"))) {
				continue;
			}

			InputStream inputStream;
			Sheet sheet;
			try {
				inputStream = file.getInputStream();
				Workbook workbook = filename.toLowerCase().endsWith(".xlsx") ? new XSSFWorkbook(inputStream)
						: new HSSFWorkbook(inputStream);
				sheet = workbook.getSheetAt(0);
			} catch (IOException e) {
				throw new ExcelProcessingException("Excel processing exception !!! : ", filename);
			}

			if (filename.contains(" - ")) {
				filename = filename.substring(0, filename.indexOf(" - ")).trim();
			}
			System.out.println("updated filename:" + filename);

			FilesFactory filesFactory = new FilesFactory();

			MutualFundFile mutualFundFile = filesFactory.getFile(filename);

			if (mutualFundFile == null) {
				continue;

			}

			MutualFundDTO mutualFundDTO = mutualFundFile.extractFile(sheet);
			log.info("{}", mutualFundDTO.getEquity().size());

			mutualFundDTO.setCreatedBy(userName);

			log.info("Size of {} : {}", mutualFundDTO.getFundName() + "|" + mutualFundDTO.getDateOfPortfolio(),
					mutualFundDTO.getEquity().size());

			handleDBOperationToSaveFileDetails(mutualFundDTO);

			// Save file to folder
			try {
				saveFileToFolder(file, "uploaded-files/success");
			} catch (IOException e) {
				throw new ExcelProcessingException("Excel processing exception !!! : ", filename);
			}

		}
		return ResponseEntity.ok("All Files processed succesfully..");
	}

	// To get all the funds
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
		// Construct the full file path where the file will be saved
		// (folderPath/filename)
		String filePath = folderPath + File.separator + file.getOriginalFilename();
		// Use try-with-resources to automatically close streams after use
		try (InputStream in = file.getInputStream(); // InputStream from the uploaded MultipartFile
				OutputStream out = new FileOutputStream(filePath)) { // OutputStream to the destination file

			byte[] buffer = new byte[1024];// Create a 1 KB buffer for efficient reading/writing
			int length;
			// Read from the input stream into the buffer and write to the output stream
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length); // Write only the number of bytes actually read
			}
		}
	}

	@Transactional
	private void handleDBOperationToSaveFileDetails(MutualFundDTO mutualFundDTO) {
		try {
			int fundid = fundRepository.insertFundIfNotExists(mutualFundDTO.getFundName(), mutualFundDTO.getFundType(),
					mutualFundDTO.getCreatedBy());

			if (mutualFundDTO.getEquity() != null) {
				for (EquityDTO equity : mutualFundDTO.getEquity()) {
					// log.info("MarketValue:{}", equity.getMarketValue());
					int instrumentId = instrumentRepository.insertInstrumentIfNotExists(equity.getIsin(),
							equity.getInstrumentName(), equity.getSector(), mutualFundDTO.getCreatedBy());
					int holdingId = holdingsRepository.insertHoldingIfNotExists(fundid, instrumentId,
							mutualFundDTO.getCreatedBy());
					holdingTransactionsRepository.upsertTransaction(holdingId, mutualFundDTO.getDateOfPortfolio(),
							equity.getQuantity(), equity.getMarketValue(), equity.getNetAsset(),
							mutualFundDTO.getCreatedBy());
				}
			}
		} catch (DataIntegrityViolationException e) {
			throw new DBOperationFailureException("Data integrity violation while doing DB Operation");
		} catch (DataAccessException e) {
			throw new DBOperationFailureException("Database access error");
		}
	}

}
