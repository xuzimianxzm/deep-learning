package com.xuzimian.dl.od

import ai.djl.ModelException
import ai.djl.inference.Predictor
import ai.djl.modality.cv.Image
import ai.djl.modality.cv.output.DetectedObjects
import ai.djl.repository.zoo.Criteria
import ai.djl.repository.zoo.ZooModel
import ai.djl.training.util.ProgressBar
import java.io.IOException

object DetectObjectModelFactory {

    /**
     * ModelUrl: djl://ai.djl.pytorch/yolov5s dependency the library ai.djl.pytorch:pytorch-model-zoo
     */
    @Throws(IOException::class, ModelException::class)
    fun loadYolovModel(): ZooModel<Image, DetectedObjects> {
        val criteria =
            Criteria.builder()
                .setTypes(Image::class.java, DetectedObjects::class.java)
                .optModelUrls("djl://ai.djl.pytorch/yolov5s")
                .optProgress(ProgressBar())
                .build()

        return criteria.loadModel()
    }


    val predictor: Predictor<Image, DetectedObjects> by lazy {
        val criteria: Criteria<Image, DetectedObjects> =
            Criteria.builder()
                .setTypes(Image::class.java, DetectedObjects::class.java)
                .optArtifactId("ssd")
                .optProgress(ProgressBar()).build()
        val model = criteria.loadModel()

        model.newPredictor()
    }
}