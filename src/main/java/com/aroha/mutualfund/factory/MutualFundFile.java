package com.aroha.mutualfund.factory;

import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;

public interface MutualFundFile {

	List<String[]> extractFile(Sheet sheet);
}
