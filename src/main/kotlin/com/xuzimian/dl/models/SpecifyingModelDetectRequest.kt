package com.xuzimian.dl.models

import jakarta.validation.constraints.NotBlank
import org.jetbrains.annotations.NotNull
import org.springframework.web.multipart.MultipartFile

class SpecifyingModelDetectRequest {

    @NotBlank(message = "type不能为空")
    lateinit var type: String

    @NotBlank(message = "fullName不能为空")
    lateinit var name: String

    var properties: String? = null

    @NotNull("图片不能为空")
    lateinit var file: MultipartFile

    fun key(): String {
        return "$type-$name-$properties"
    }

    override fun toString(): String {
        return "type='$type', name='$name', properties=$properties"
    }
}