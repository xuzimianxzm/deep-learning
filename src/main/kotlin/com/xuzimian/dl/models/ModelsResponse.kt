package com.xuzimian.dl.models

class ModelsResponse {
    lateinit var basicModels: Map<String, List<DeepLearningModel>>
}

class DeepLearningModel {
    lateinit var type: String
    lateinit var name: String
    lateinit var properties: Map<String, String>
}