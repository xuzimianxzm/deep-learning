package com.xuzimian.dl.models

import jakarta.annotation.Resource
import org.springframework.web.bind.annotation.GetMapping
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
}