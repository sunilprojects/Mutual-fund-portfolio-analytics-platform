
package com.aroha.mutualfund.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import com.aroha.mutualfund.dto.HoldingDetail;

import com.aroha.mutualfund.dto.FundsResponceDTO;

public interface MutualFundService {
	String processFundFile(MultipartFile[] files);

	List<FundsResponceDTO> getAllFunds();

	List<HoldingDetail> getFundHoldings(int fundId);
}
