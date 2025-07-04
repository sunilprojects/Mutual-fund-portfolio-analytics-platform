package com.aroha.mutualfund.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aroha.mutualfund.dto.EquityDTO;

import com.aroha.mutualfund.dto.FundsResponceDTO;

import com.aroha.mutualfund.dto.HoldingDetail;

import com.aroha.mutualfund.dto.MutualFundDTO;
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
	public String processFundFile(MultipartFile[] files,String userName) {
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
				// TODO: pass Multipart file name
				MutualFundFile mutualFundFile = filesFactory.getFile(filename);
				
<<<<<<< HEAD
				MutualFundDTO fieldList = mutualFundFile.extractFile(sheet);

				log.info("{}", fieldList.getEquity().size());
				
=======
				if(mutualFundFile==null) {
					continue;
				}
				// TODO: handle null
				// TODO: pass Sheet

>>>>>>> 53e4651aa86b6cd1566189e881bdb78d875e029b
				MutualFundDTO mutualFundDTO = mutualFundFile.extractFile(sheet);
				processedFiles.add(filename);
				log.info("{}", mutualFundDTO.getEquity().size());

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
				skippedFiles.add(filename);
				e.printStackTrace();
			}
		}

		return "Processed Files: " + processedFiles + "\nSkipped Files:" + skippedFiles+  " !!!!!";
	}

	@Override
	public List<FundsResponceDTO> getAllFunds() {
		return fundRepository.getAllFunds();
	}

	public List<HoldingDetail> getFundHoldings(int fundId) {
		return holdingsRepository.getHoldingsByFundId(fundId);
	}

}
