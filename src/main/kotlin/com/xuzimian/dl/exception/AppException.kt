package com.xuzimian.dl.exception

import org.springframework.http.HttpStatus

class AppException(message: String, val status: HttpStatus, val code: Int, override val cause: Exception?) :
    RuntimeException(message, cause) {

    companion object {
        fun getAppException(errorCode: ErrorCode): AppException {
            return AppException(errorCode.message(), errorCode.status(), errorCode.code(), null)
        }

        fun getAppException(errorCode: ErrorCode, message: String): AppException {
            return AppException(message, errorCode.status(), errorCode.code(), null)
        }
    }
}
