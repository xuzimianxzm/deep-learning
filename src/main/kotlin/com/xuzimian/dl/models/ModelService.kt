package com.xuzimian.dl.models

import ai.djl.Application
import ai.djl.basicmodelzoo.BasicModelZoo
import ai.djl.inference.Predictor
import ai.djl.modality.Classifications
import ai.djl.modality.cv.Image
import ai.djl.modality.cv.ImageFactory
import ai.djl.repository.zoo.Criteria
import ai.djl.training.util.ProgressBar
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.Resource
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap


@Service
class ModelService {

    private val logger = KotlinLogging.logger {}

    @Resource
    private lateinit var objectMapper: ObjectMapper

    private val models = ConcurrentHashMap<String, Predictor<Image, Any>>()

    fun getModels(): ModelsResponse {
        return ModelsResponse().apply {
            basicModels = BasicModelZoo.listModels().map {
                it.key.toString() to it.value.map { artifact ->
                    DeepLearningModel().apply {
                        type = it.key.path
                        name = artifact.name
                        properties = artifact.properties
                    }
                }.toList()
            }.toMap()
        }
    }

    fun specifyingModelDetect(request: SpecifyingModelDetectRequest): String {
        val predictor = models.getOrDefault(request.key(), predictor(request))
        val image = ImageFactory.getInstance().fromInputStream(request.file.inputStream)
        val classifications = predictor.predict(image)
        return classifications.toString()
    }

    private fun predictor(
        request: SpecifyingModelDetectRequest
    ): Predictor<Image, Classifications> {
        logger.info { "根据给定参数加载指定模型:$request" }

        val application = Application.of(request.type)

        val criteriaBuilder = Criteria.builder()
            .optProgress(ProgressBar())
            .optApplication(application)
            .optModelName(request.name)
            .setTypes(Image::class.java, Classifications::class.java)
            .optProgress(ProgressBar())

        if (!request.properties.isNullOrEmpty()) {
            criteriaBuilder.optFilters(
                objectMapper.readValue(
                    request.properties, object : TypeReference<Map<String, String>>() {}
                ))
        }

        val criteria = criteriaBuilder.build()

        val zooModel = criteria.loadModel()

        return zooModel.newPredictor()
    }
}