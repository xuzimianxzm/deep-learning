package com.xuzimian.dl.models

import jakarta.annotation.Resource
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/models")
class ModelController {

    @Resource
    private lateinit var modelService: ModelService

    @GetMapping
    fun getModels(): ModelsResponse {
        return modelService.getModels()
    }

    @PostMapping("/specifyingModelDetect", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun specifyingModelDetect(@Validated request: SpecifyingModelDetectRequest): String {
        return modelService.specifyingModelDetect(request)
    }
}