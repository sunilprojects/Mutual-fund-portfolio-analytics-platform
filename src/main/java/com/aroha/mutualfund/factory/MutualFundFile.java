package com.aroha.mutualfund.factory;

import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;

import com.aroha.mutualfund.pojo.RowEntityBundle;

public interface MutualFundFile {

	List<RowEntityBundle> extractFile(Sheet sheet);
}
