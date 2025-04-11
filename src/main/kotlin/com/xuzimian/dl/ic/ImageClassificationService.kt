package com.xuzimian.dl.ic

import ai.djl.inference.Predictor
import ai.djl.metric.Metrics
import ai.djl.modality.Classifications
import ai.djl.modality.cv.Image
import ai.djl.modality.cv.ImageFactory
import ai.djl.repository.zoo.ZooModel
import jakarta.annotation.Resource
import mu.KotlinLogging
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.io.InputStream


@Service
class ImageClassificationService {

    private val logger = KotlinLogging.logger {}

    @Lazy
    @Resource
    private lateinit var resNetModel: ZooModel<Image, Classifications>

    private val metrics: Metrics by lazy {
        Metrics()
    }

    private val imageClassificationPredictor: Predictor<Image, Classifications> by lazy {
        val predictor = resNetModel.newPredictor()
        predictor.setMetrics(metrics)

        predictor
    }


    fun imageClassify(inputStream: InputStream): String {
        val image = ImageFactory.getInstance().fromInputStream(inputStream)
        val classifications = imageClassificationPredictor.predict(image)

        val inferenceMean = metrics.mean("Inference")
        val inferenceP90: Number = metrics.percentile("Inference", 90).value

        logger.info { "推理请求的平均延迟:$inferenceMean 和 p90延迟:$inferenceP90" }

        return classifications.toString()
    }
}