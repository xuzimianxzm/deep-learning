package com.xuzimian.dl.ocr

import ai.djl.inference.Predictor
import ai.djl.modality.Classifications
import ai.djl.modality.cv.Image
import ai.djl.modality.cv.output.DetectedObjects
import ai.djl.repository.zoo.ZooModel
import com.xuzimian.dl.config.PrefixThreadName
import com.xuzimian.dl.exception.AppException
import com.xuzimian.dl.ocr.LabelDetectionErrorCode.OCR_DETECTOR_NOT_EXIST_IN_CURRENT_THREAD
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * 解决并发情况下，图像识别算法内存消耗过高导致程序崩溃的问题。采用线程池固定线程帮顶固定的图像识别器方式来服用图像识别和多线程，
 * 降低开销和用阻塞方式来应对超出负载的并发情况。
 * 设计方案：
 * Detection Class 有三个 Predictor属性：recognizer,detector,rotateClassifier
 * 每个 AI-Executor 线程 拥有一个 Detection Class 实例，可以考虑 ThreadLocal 或者 ConcurrentHashMap<ThreadName,Detection>
 * 采用 @PostConstruct 来根据AI-Executor线程数量来初始化 固定数量的 Detection 实例
 */
@Component
class OCRDetector {

    private val logger = KotlinLogging.logger {}

    @Value("\${app.aiThreadPoolSize:#{5}}")
    private var aiThreadPoolSize = 5

    @Value("\${app.model.rootDirectory:#{null}}")
    private var modelRootDirectory: String? = null

    private val recognizerModel: ZooModel<Image, String> by lazy {
        PaddleOCRFactory.createTextRecognizerModel(modelRootDirectory)
    }

    private val textAreaDetectorModel: ZooModel<Image, DetectedObjects> by lazy {
        PaddleOCRFactory.createTextAreaDetectorModel(modelRootDirectory)
    }

    private val rotateClassifierModel: ZooModel<Image, Classifications> by lazy {
        PaddleOCRFactory.createTextRotateClassifierModel(modelRootDirectory)
    }

    private val recognizers: ConcurrentMap<String, Predictor<Image, String>> = ConcurrentHashMap()

    private val textAreaDetectors: ConcurrentMap<String, Predictor<Image, DetectedObjects>> = ConcurrentHashMap()

    private val rotateClassifiers: ConcurrentMap<String, Predictor<Image, Classifications>> = ConcurrentHashMap()

    private val initialization: Unit by lazy {
        logger.info { "开始OCR 模型初始化..." }
        for (index in 1..aiThreadPoolSize) {
            recognizers["$PrefixThreadName$index"] = recognizerModel.newPredictor()
            textAreaDetectors["$PrefixThreadName$index"] = textAreaDetectorModel.newPredictor()
            rotateClassifiers["$PrefixThreadName$index"] = rotateClassifierModel.newPredictor()
        }
    }

    fun predictTextArea(img: Image): DetectedObjects {
        initialization
        val textAreaDetector = textAreaDetectors[Thread.currentThread().name] ?: throw AppException.getAppException(
            OCR_DETECTOR_NOT_EXIST_IN_CURRENT_THREAD
        )
        return textAreaDetector.predict(img)
    }

    fun predictRotate(img: Image): Classifications {
        initialization
        val rotateClassifier = rotateClassifiers[Thread.currentThread().name] ?: throw AppException.getAppException(
            OCR_DETECTOR_NOT_EXIST_IN_CURRENT_THREAD
        )
        return rotateClassifier.predict(img)
    }

    fun predictRecognize(img: Image): String {
        initialization
        val recognizer = recognizers[Thread.currentThread().name] ?: throw AppException.getAppException(
            OCR_DETECTOR_NOT_EXIST_IN_CURRENT_THREAD
        )
        return recognizer.predict(img)
    }
}
