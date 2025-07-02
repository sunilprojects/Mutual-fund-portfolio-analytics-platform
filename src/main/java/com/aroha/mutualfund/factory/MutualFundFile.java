package com.aroha.mutualfund.factory;

import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;

import com.aroha.mutualfund.dto.MutualFundDTO;

public interface MutualFundFile {

	MutualFundDTO extractFile(Sheet sheet) ;
}
