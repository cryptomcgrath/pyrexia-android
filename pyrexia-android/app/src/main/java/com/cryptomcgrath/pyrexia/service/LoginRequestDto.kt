package com.cryptomcgrath.pyrexia.service


internal data class LoginRequestDto(
    val email: String,
    val password: String
)