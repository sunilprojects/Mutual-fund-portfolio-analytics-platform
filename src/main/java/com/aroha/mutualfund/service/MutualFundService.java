package com.aroha.mutualfund.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface MutualFundService {
         String processFundFile(MultipartFile[] files);

		List<String> getSectorsByFundId(int fundId);
		
}
