
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

import com.aroha.mutualfund.dto.MutualFundDTO;
import com.aroha.mutualfund.factory.FilesFactory;
import com.aroha.mutualfund.factory.MutualFundFile;
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
				// TODO: handle null
				// TODO: pass Sheet
				MutualFundDTO fieldList = mutualFundFile.extractFile(sheet);
				
				log.info("{}", fieldList.getEquity().size());

			} catch (Exception e) {
				skippedFiles.add(filename);
				e.printStackTrace();
			}
		}

		return "Processed Files: " + processedFiles + "\nSkipped Files: " + skippedFiles;
	}

}
