package com.xuzimian.dl.ocr

import ai.djl.inference.Predictor
import ai.djl.modality.Classifications
import ai.djl.modality.cv.Image
import ai.djl.modality.cv.output.DetectedObjects
import ai.djl.repository.zoo.ZooModel
import com.xuzimian.dl.exception.AppException
import com.xuzimian.dl.ocr.LabelDetectionErrorCode.*
import jakarta.annotation.PostConstruct
import jakarta.annotation.Resource
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

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

    @Value("\${app.aiThreadPoolSize:#{5}}")
    private var aiThreadPoolSize = 5

    @Resource
    private lateinit var recognizerModel: ZooModel<Image, String>

    @Resource
    private lateinit var textAreaDetectorModel: ZooModel<Image, DetectedObjects>

    @Resource
    private lateinit var rotateClassifierModel: ZooModel<Image, Classifications>

    private val recognizers: ConcurrentMap<String, Predictor<Image, String>> = ConcurrentHashMap()

    private val textAreaDetectors: ConcurrentMap<String, Predictor<Image, DetectedObjects>> = ConcurrentHashMap()

    private val rotateClassifiers: ConcurrentMap<String, Predictor<Image, Classifications>> = ConcurrentHashMap()

    @PostConstruct
    fun init() {
        for (index in 1 .. aiThreadPoolSize) {
            recognizers["$PrefixThreadName$index"] = recognizerModel.newPredictor()
            textAreaDetectors["$PrefixThreadName$index"] = textAreaDetectorModel.newPredictor()
            rotateClassifiers["$PrefixThreadName$index"] = rotateClassifierModel.newPredictor()
        }
    }

    fun predictTextArea(img: Image): DetectedObjects {
        val textAreaDetector = textAreaDetectors[Thread.currentThread().name] ?: throw AppException.getAppException(
            OCR_DETECTOR_NOT_EXIST_IN_CURRENT_THREAD
        )
        return textAreaDetector.predict(img)
    }

    fun predictRotate(img: Image): Classifications {
        val rotateClassifier = rotateClassifiers[Thread.currentThread().name] ?: throw AppException.getAppException(
            OCR_DETECTOR_NOT_EXIST_IN_CURRENT_THREAD
        )
        return rotateClassifier.predict(img)
    }

    fun predictRecognize(img: Image): String {
        val recognizer = recognizers[Thread.currentThread().name] ?: throw AppException.getAppException(
            OCR_DETECTOR_NOT_EXIST_IN_CURRENT_THREAD
        )
        return recognizer.predict(img)
    }
}
