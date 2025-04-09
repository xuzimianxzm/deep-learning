package com.xuzimian.dl.exception

import org.springframework.http.HttpStatus

enum class CommonErrorCode(private val code: Int, private val status: HttpStatus, private val message: String) :
    ErrorCode {
    SUCCESS(200, HttpStatus.OK, "成功"),
    PARAMETER_ERROR(400, HttpStatus.BAD_REQUEST, "参数格式异常"),
    TOKEN_ERROR(401, HttpStatus.UNAUTHORIZED, "未知的请求，或Token已失效，请重新登录"),
    REFRESH_TOKEN_EXPIRED(402, HttpStatus.UNAUTHORIZED, "Refresh token 校验异常，不存在或已过期。请重新登录再试"),
    ID_UNEXIST_ERROR(404, HttpStatus.NOT_FOUND, "参数异常（ID不存在）"),
    VERSION_ERROR(409, HttpStatus.CONFLICT, "version冲突，请重试"),
    SERVER_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR, "服务器异常"),
    DATE_FORMAT_ERROR(600, HttpStatus.BAD_REQUEST, "日期格式异常，格式为：yyyy-MM-dd  如：2020-01-01"),
    PASSWORD_ERROR(601, HttpStatus.BAD_REQUEST, "密码输入错误"),
    ;

    override fun status(): HttpStatus {
        return this.status
    }

    override fun code(): Int {
        return this.code
    }

    override fun message(): String {
        return this.message
    }
}
