package com.xuzimian.dl.ic

import ai.djl.inference.Predictor
import ai.djl.modality.Classifications
import ai.djl.modality.cv.Image
import ai.djl.modality.cv.ImageFactory
import ai.djl.repository.zoo.ZooModel
import jakarta.annotation.Resource
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.io.InputStream


@Service
class ImageClassificationService {

    @Lazy
    @Resource
    private lateinit var resNetModel: ZooModel<Image, Classifications>

    private val imageClassificationPredictor: Predictor<Image, Classifications> by lazy {
        resNetModel.newPredictor()
    }


    fun imageClassify(inputStream: InputStream): String {
        val image = ImageFactory.getInstance().fromInputStream(inputStream)
        val classifications = imageClassificationPredictor.predict(image)

        return classifications.toString()
    }
}