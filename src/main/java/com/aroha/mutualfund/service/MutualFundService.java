package com.aroha.mutualfund.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.aroha.mutualfund.dto.HoldingDetail;

public interface MutualFundService {
	String processFundFile(MultipartFile[] file,String userName);

	List<HoldingDetail> getFundHoldings(int fundId) ;

   
}
