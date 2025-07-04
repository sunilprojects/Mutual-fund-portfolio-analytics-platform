package com.aroha.mutualfund.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aroha.mutualfund.dto.EquityDTO;
import com.aroha.mutualfund.dto.FundsResponceDTO;
import com.aroha.mutualfund.dto.HoldingDetail;
import com.aroha.mutualfund.dto.MutualFundDTO;
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

	//Constructor injection
	public MutualFundServiceImpl(FundRepository mutualFundRepository, InstrumentRepository instrumentRepository,
			HoldingsRepository holdingsRepository, HoldingTransactionsRepository holdingTransactionsRepository) {
		this.fundRepository = mutualFundRepository;
		this.instrumentRepository = instrumentRepository;
		this.holdingsRepository = holdingsRepository;
		this.holdingTransactionsRepository = holdingTransactionsRepository;
	}

	//To process Uplaoded Files
	@Override
	public String processFundFile(MultipartFile[] files, String userName) {
		if (files == null || files.length == 0) {
			return "No files uploaded";
		}

		List<String> processedFiles = new ArrayList<>();
		List<String> skippedFiles = new ArrayList<>();

		for (MultipartFile file : files) {
			String filename = file.getOriginalFilename();

			if (filename == null
					|| (!filename.toLowerCase().endsWith(".xls") && !filename.toLowerCase().endsWith(".xlsx"))) {
				skippedFiles.add(filename);
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

				if (mutualFundFile == null) {
					continue;
				}

				MutualFundDTO mutualFundDTO = mutualFundFile.extractFile(sheet);
				mutualFundDTO.setCreatedBy(userName);

				processedFiles.add(filename);
				log.info("Size of {} : {}", mutualFundDTO.getFundName() + "|" + mutualFundDTO.getDateOfPortfolio(),
						mutualFundDTO.getEquity().size());

				handleDBOperationToSaveFileDetails(mutualFundDTO);

			} catch (Exception e) {
				skippedFiles.add(filename);
				e.printStackTrace();
			}
		}

		return "Processed Files: " + processedFiles + "\nSkipped Files:" + skippedFiles + " !!!!!";
	}

	// To get all the funds
	@Override
	public List<FundsResponceDTO> getAllFunds() {
		return fundRepository.getAllFunds();
	}

	public List<HoldingDetail> getFundHoldings(int fundId) {
		return holdingsRepository.getHoldingsByFundId(fundId);
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
