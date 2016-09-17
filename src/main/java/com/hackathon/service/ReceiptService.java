package com.hackathon.service;

import org.springframework.web.multipart.MultipartFile;

public interface ReceiptService {

	String processReceiptImage(MultipartFile file);

}
