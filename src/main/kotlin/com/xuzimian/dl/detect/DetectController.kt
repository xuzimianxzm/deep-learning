package com.xuzimian.dl.detect

import com.xuzimian.dl.ic.ImageClassificationService
import com.xuzimian.dl.ocr.saveAsPNG
import com.xuzimian.dl.od.ObjectDetectionService
import jakarta.annotation.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream


@RestController
@RequestMapping("/ai")
class DetectController {

    @Resource
    private lateinit var detectService: DetectService

    @Resource
    private lateinit var imageClassificationService: ImageClassificationService

    @Resource
    private lateinit var objectDetectionService: ObjectDetectionService

    @PostMapping("/detectText", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun detectText(@RequestParam("image") file: MultipartFile): List<String> {
        return detectService.detectText(file)
    }

    @PostMapping("/markTextOnImage", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun markTextOnImage(@RequestParam("image") file: MultipartFile): ResponseEntity<ByteArray> {
        var image = detectService.markTextOnImage(file)

        val outputStream = ByteArrayOutputStream()
        image.saveAsPNG(outputStream)
        var byteArray = outputStream.toByteArray()

        val headers = HttpHeaders()
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=detectImage.png")
        headers.contentType = MediaType.IMAGE_PNG
        return ResponseEntity.ok()
            .headers(headers)
            .contentLength(byteArray.size.toLong())
            .body(byteArray)
    }

    @PostMapping("/imageClassify", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun imageClassify(@RequestParam("image") file: MultipartFile): String {
        return imageClassificationService.imageClassify(file.inputStream)
    }

    @PostMapping("/objectDetect", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun objectDetect(@RequestParam("image") file: MultipartFile): ResponseEntity<ByteArray> {
        var image = objectDetectionService.objectDetect(file.inputStream).get()

        val outputStream = ByteArrayOutputStream()
        image.saveAsPNG(outputStream)
        var byteArray = outputStream.toByteArray()

        val headers = HttpHeaders()
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=detectImage.png")
        headers.contentType = MediaType.IMAGE_PNG
        return ResponseEntity.ok()
            .headers(headers)
            .contentLength(byteArray.size.toLong())
            .body(byteArray)
    }
}