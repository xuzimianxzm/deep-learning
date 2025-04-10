package com.xuzimian.dl.detect

import jakarta.annotation.Resource
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/ai")
class DetectController {

    @Resource
    private lateinit var detectService: DetectService

    @PostMapping("/detectText", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun detectText(@RequestParam("image") file: MultipartFile): List<String> {
        return detectService.detectText(file)
    }
}