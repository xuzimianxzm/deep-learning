package com.xuzimian.dl.od

import ai.djl.inference.Predictor
import ai.djl.modality.cv.Image
import ai.djl.modality.cv.ImageFactory
import ai.djl.modality.cv.output.DetectedObjects
import ai.djl.repository.zoo.Criteria
import ai.djl.training.util.ProgressBar
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.InputStream
import java.util.concurrent.CompletableFuture


@Service
class ObjectDetectionService {

    private val logger = KotlinLogging.logger {}

    private val predictor: Predictor<Image, DetectedObjects> by lazy {
        val criteria: Criteria<Image, DetectedObjects> =
            Criteria.builder().setTypes(Image::class.java, DetectedObjects::class.java).optArtifactId("ssd")
                .optProgress(ProgressBar()).build()
        val model = criteria.loadModel()

        model.newPredictor()
    }

    fun objectDetect(inputStream: InputStream): CompletableFuture<BufferedImage> {
        logger.info { "识别并标记图像: ${Thread.currentThread().name} " }
        return CompletableFuture.completedFuture(
            objectDetectOnImage(
                ImageFactory.getInstance().fromInputStream(inputStream)
            )
        )
    }

    private fun objectDetectOnImage(img: Image): BufferedImage {
        var detections = predictor.predict(img)
        logger.info { "识别目标结果: $detections" }

        img.drawBoundingBoxes(detections)
        return img.wrappedImage as BufferedImage
    }
}