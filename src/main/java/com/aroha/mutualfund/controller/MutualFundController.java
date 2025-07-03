package com.aroha.mutualfund.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aroha.mutualfund.constant.EndPoints;
import com.aroha.mutualfund.service.MutualFundService;

@RestController
@RequestMapping(EndPoints.API_V1_FUNDS)
public class MutualFundController {
	 private MutualFundService mutualFundService;
	
	public MutualFundController(MutualFundService mutualFundService) {
		this.mutualFundService = mutualFundService;
	}
	
	@PostMapping("/upload")
	public ResponseEntity<String> uploadFundFile(@RequestParam("files") MultipartFile[] files) {
		String result = mutualFundService.processFundFile(files);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/{fundId}/sectors")
	public ResponseEntity<List<String>> getSectorsByFundId(@PathVariable int fundId) {
		List<String> sectors=mutualFundService.getSectorsByFundId(fundId);
		return ResponseEntity.ok(sectors);
	}

	
}
