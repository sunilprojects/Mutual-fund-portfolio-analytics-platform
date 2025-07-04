package com.aroha.mutualfund.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import com.aroha.mutualfund.dto.HoldingDetail;

import com.aroha.mutualfund.dto.FundsResponceDTO;

public interface MutualFundService {
<<<<<<< HEAD
	String processFundFile(MultipartFile[] file,String userName);
=======
	String processFundFile(MultipartFile[] files);
>>>>>>> 53e4651aa86b6cd1566189e881bdb78d875e029b

	List<FundsResponceDTO> getAllFunds();

	List<HoldingDetail> getFundHoldings(int fundId);
}
