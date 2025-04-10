package com.xuzimian.dl.ocr

import ai.djl.inference.Predictor
import ai.djl.modality.Classifications
import ai.djl.modality.cv.Image
import ai.djl.modality.cv.output.DetectedObjects
import ai.djl.repository.zoo.ZooModel
import com.xuzimian.dl.config.PrefixThreadName
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Lazy
@Configuration
class PaddleOCRConfig {

    private val logger = KotlinLogging.logger {}

    @Value("\${app.model.rootDirectory:#{null}}")
    private var modelRootDirectory: String? = null

    @Value("\${app.aiThreadPoolSize:#{5}}")
    private var aiThreadPoolSize = 5

    private val recognizerModel: ZooModel<Image, String> by lazy {
        PaddleOCRFactory.createTextRecognizerModel(modelRootDirectory)
    }

    private val textAreaDetectorModel: ZooModel<Image, DetectedObjects> by lazy {
        PaddleOCRFactory.createTextAreaDetectorModel(modelRootDirectory)
    }

    private val rotateClassifierModel: ZooModel<Image, Classifications> by lazy {
        PaddleOCRFactory.createTextRotateClassifierModel(modelRootDirectory)
    }


    @Bean
    @Lazy
    fun getRecognizers(): ConcurrentMap<String, Predictor<Image, String>> {
        logger.info { "开始 OCR:Recognizer 模型初始化..." }

        val recognizers = ConcurrentHashMap<String, Predictor<Image, String>>()
        for (index in 1..aiThreadPoolSize) {
            recognizers["$PrefixThreadName$index"] = recognizerModel.newPredictor()
        }
        return recognizers
    }

    @Bean
    @Lazy
    fun getTextAreaDetectors(): ConcurrentMap<String, Predictor<Image, DetectedObjects>> {
        logger.info { "开始 OCR:TextAreaDetector 模型初始化..." }

        val textAreaDetectors = ConcurrentHashMap<String, Predictor<Image, DetectedObjects>>()
        for (index in 1..aiThreadPoolSize) {
            textAreaDetectors["$PrefixThreadName$index"] = textAreaDetectorModel.newPredictor()
        }
        return textAreaDetectors
    }

    @Bean
    @Lazy
    fun getRotateClassifiers(): ConcurrentMap<String, Predictor<Image, Classifications>> {
        logger.info { "开始 OCR:RotateClassifier 模型初始化..." }

        val rotateClassifier = ConcurrentHashMap<String, Predictor<Image, Classifications>>()
        for (index in 1..aiThreadPoolSize) {
            rotateClassifier["$PrefixThreadName$index"] = rotateClassifierModel.newPredictor()
        }
        return rotateClassifier
    }
}
