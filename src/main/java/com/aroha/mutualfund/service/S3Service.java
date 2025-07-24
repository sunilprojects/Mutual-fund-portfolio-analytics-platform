package com.aroha.mutualfund.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

	void uploadFile(MultipartFile file, String key);
}
