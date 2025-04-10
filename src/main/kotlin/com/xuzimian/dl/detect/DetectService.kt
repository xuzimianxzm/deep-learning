package com.xuzimian.dl.detect

import ai.djl.modality.cv.ImageFactory
import com.xuzimian.dl.ocr.PaddleOCRService
import jakarta.annotation.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.awt.image.BufferedImage

@Service
class DetectService {

    @Resource
    private lateinit var paddleOCRService: PaddleOCRService

    fun detectText(file: MultipartFile): List<String> {
        return paddleOCRService.getImageTextsAsync(file.inputStream.readAllBytes()).get()
    }

    fun markTextOnImage(file: MultipartFile): BufferedImage {
        return paddleOCRService.markTextOnImageAsync(ImageFactory.getInstance().fromInputStream(file.inputStream)).get()
    }
}