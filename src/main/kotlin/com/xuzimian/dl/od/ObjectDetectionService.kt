package com.xuzimian.dl.od

import ai.djl.modality.cv.Image
import ai.djl.modality.cv.ImageFactory
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.CompletableFuture


@Service
class ObjectDetectionService {

    private val logger = KotlinLogging.logger {}


    fun objectDetect(inputStream: InputStream): CompletableFuture<ByteArray> {
        logger.info { "识别并标记图像: ${Thread.currentThread().name} " }
        return CompletableFuture.completedFuture(
            objectDetectOnImage(
                ImageFactory.getInstance().fromInputStream(inputStream)
            )
        )
    }

    private fun objectDetectOnImage(img: Image): ByteArray {
        var detections = DetectObjectModelFactory.predictor.predict(img)
        logger.info { "识别目标结果: $detections" }

        img.drawBoundingBoxes(detections)

        val outputStream = ByteArrayOutputStream()
        img.save(outputStream, "jpg")
        return outputStream.toByteArray()
    }
}