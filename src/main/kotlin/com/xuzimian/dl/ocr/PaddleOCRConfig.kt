package com.xuzimian.dl.ocr

import ai.djl.modality.Classifications
import ai.djl.modality.cv.Image
import ai.djl.modality.cv.output.DetectedObjects
import ai.djl.repository.zoo.ZooModel
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Lazy
@Configuration
class PaddleOCRConfig {

    private val logger = KotlinLogging.logger {}

    @Value("\${app.model.rootDirectory:#{null}}")
    private var modelRootDirectory: String? = null


    @Bean
    fun getRecognizerModel(): ZooModel<Image, String> {
        return PaddleOCRFactory.createTextRecognizerModel(modelRootDirectory)
    }

    @Bean
    fun getDetectorModel(): ZooModel<Image, DetectedObjects> {
        return PaddleOCRFactory.createTextAreaDetectorModel(modelRootDirectory)
    }

    @Bean
    fun getRotateClassifierModel(): ZooModel<Image, Classifications> {
        return PaddleOCRFactory.createTextRotateClassifierModel(modelRootDirectory)
    }
}
