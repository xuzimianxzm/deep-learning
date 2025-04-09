package com.xuzimian.dl.ocr

import com.xuzimian.dl.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class LabelDetectionErrorCode : ErrorCode {
    USER_ID_IS_REQUIRED {
        override fun code() = 400
        override fun status() = HttpStatus.BAD_REQUEST
        override fun message() = "userId是必须的"
    },
    LABEL_ID_NOT_FOUND {
        override fun code() = 404
        override fun status() = HttpStatus.NOT_FOUND
        override fun message() = "标签Id不存在"
    },
    TEMPLATE_ID_NOT_FOUND {
        override fun code() = 404
        override fun status() = HttpStatus.NOT_FOUND
        override fun message() = "模板Id不存在"
    },
    RECODE_ID_NOT_FOUND {
        override fun code() = 404
        override fun status() = HttpStatus.NOT_FOUND
        override fun message() = "记录Id不存在"
    },
    TEMPLATE_IMAGE_ID_NOT_FOUND {
        override fun code() = 404
        override fun status() = HttpStatus.NOT_FOUND
        override fun message() = "模板图片refId不存在"
    },
    USER_TEMPLATE_EXCEEDS_LIMIT {
        override fun code() = 400
        override fun status() = HttpStatus.BAD_REQUEST
        override fun message() = "用户模板数量超出限制"
    },
    CAN_NOT_FIND_CHECK_TYPE_IN_DETECTION_MAP {
        override fun code() = 400
        override fun status() = HttpStatus.BAD_REQUEST
        override fun message() = "不能在策略Map中找到对应的策略:"
    },
    MUST_CONTAIN_PERMANENT_TAGS {
        override fun code() = 400
        override fun status() = HttpStatus.BAD_REQUEST
        override fun message() = "必须包含耐久标签"
    },
    ENUM_VALUE_CONVERTER {
        override fun code() = 500
        override fun status() = HttpStatus.BAD_REQUEST
        override fun message() = "枚举值不正确"
    },
    OCR_DETECTOR_NOT_EXIST_IN_CURRENT_THREAD {
        override fun code() = 500
        override fun status() = HttpStatus.BAD_REQUEST
        override fun message() = "当前线程不存在检测器"
    };
}