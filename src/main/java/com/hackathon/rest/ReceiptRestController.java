package com.hackathon.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hackathon.service.ReceiptService;


@RestController
@RequestMapping(path="/api/v1/receipts")
public class ReceiptRestController {

	@Autowired
	ReceiptService receiptService;
	
	@RequestMapping(method=RequestMethod.POST)
	public String postReceipt(@RequestParam("file") MultipartFile file) {
		return receiptService.processReceiptImage(file);
	}
	
}
