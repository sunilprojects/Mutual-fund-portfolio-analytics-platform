package com.aroha.mutualfund.service.impl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aroha.mutualfund.exception.AWSS3FileUploadException;
import com.aroha.mutualfund.service.S3Service;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3ServiceImpl implements S3Service {

	private final S3Client s3Client;

	@Value("${aws.s3.bucket-upload}")
	private String bucketUpload;

	@Value("${aws.s3.bucket-success}")
	private String bucketSuccess;

	public S3ServiceImpl(S3Client s3Client) {
		this.s3Client = s3Client;
	}

	@Override
	public void uploadFile(MultipartFile file, String key) {

		String fileName = file.getOriginalFilename();

		String bucketName;
		if ("SUCCESS".equals(key)) {
			bucketName = bucketSuccess;
		} else {
			bucketName = bucketUpload;
		}

		PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(fileName)
				.contentType(file.getContentType()).build();

		try {
			s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
		} catch (AwsServiceException | SdkClientException | IOException e) {

			throw new AWSS3FileUploadException("Unable to upload files to S3 bucket", fileName);
		}

		return;
	}

}
