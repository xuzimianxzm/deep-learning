package com.xuzimian.dl.models

import ai.djl.basicmodelzoo.BasicModelZoo
import org.springframework.stereotype.Service

@Service
class ModelService {

    fun getModels(): ModelsResponse {
        return ModelsResponse().apply {
            basicModels = BasicModelZoo.listModels().map {
                it.key.toString() to it.value.map { artifact ->
                    DeepLearningModel().apply {
                        type = it.key.toString()
                        fullName = artifact.name
                        properties = artifact.properties
                    }
                }.toList()
            }.toMap()
        }
    }
}