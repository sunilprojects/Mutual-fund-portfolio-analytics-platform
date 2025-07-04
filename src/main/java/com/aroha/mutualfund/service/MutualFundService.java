
package com.aroha.mutualfund.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import com.aroha.mutualfund.dto.HoldingDetail;
import com.aroha.mutualfund.dto.FundsResponceDTO;

public interface MutualFundService {
	ResponseEntity<String> processFundFile(MultipartFile[] files);

	List<FundsResponceDTO> getAllFunds();
	List<String> getSectorsByFundId(int fundId);

	List<HoldingDetail> getFundHoldings(int fundId);

	
}
