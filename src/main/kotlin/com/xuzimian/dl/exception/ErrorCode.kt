package com.xuzimian.dl.exception

import org.springframework.http.HttpStatus

interface ErrorCode {
    fun code(): Int

    fun status(): HttpStatus

    fun message(): String
}

