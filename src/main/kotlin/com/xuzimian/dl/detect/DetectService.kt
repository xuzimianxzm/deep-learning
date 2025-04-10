package com.xuzimian.dl.detect

import com.xuzimian.dl.ocr.PaddleOCRService
import jakarta.annotation.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class DetectService {

    @Resource
    private lateinit var paddleOCRService: PaddleOCRService

    fun detectText(file: MultipartFile): List<String> {
        return paddleOCRService.getImageTextsAsync(file.inputStream.readAllBytes()).get()
    }
}