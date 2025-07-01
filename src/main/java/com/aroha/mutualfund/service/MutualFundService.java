package com.aroha.mutualfund.service;

import org.springframework.web.multipart.MultipartFile;

public interface MutualFundService {
         String processFundFile(MultipartFile[] files);
}
