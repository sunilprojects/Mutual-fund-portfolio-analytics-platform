package com.aroha.mutualfund.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    @Autowired
	private MutualFundService mutualFundService;
    
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFundFile(@RequestParam("files") MultipartFile[] files) {

        String result = mutualFundService.processFundFile(files);
        return ResponseEntity.ok(result);
}
}
