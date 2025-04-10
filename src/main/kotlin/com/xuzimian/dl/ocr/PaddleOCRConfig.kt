package com.xuzimian.dl.ocr

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Lazy
@Configuration
class PaddleOCRConfig {

    private val logger = KotlinLogging.logger {}

    @Value("\${app.model.rootDirectory:#{null}}")
    private var modelRootDirectory: String? = null


//    fun getRecognizerModel(): ZooModel<Image, String> {
//        return PaddleOCRFactory.createTextRecognizerModel(modelRootDirectory)
//    }
//
//    fun getDetectorModel(): ZooModel<Image, DetectedObjects> {
//        return PaddleOCRFactory.createTextAreaDetectorModel(modelRootDirectory)
//    }
//
//    fun getRotateClassifierModel(): ZooModel<Image, Classifications> {
//        return PaddleOCRFactory.createTextRotateClassifierModel(modelRootDirectory)
//    }
}
