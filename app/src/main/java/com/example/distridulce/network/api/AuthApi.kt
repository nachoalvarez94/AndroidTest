package com.example.distridulce.network.api

import com.example.distridulce.network.dto.LoginRequestDto
import com.example.distridulce.network.dto.LoginResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    /**
     * Authenticates a user and returns a JWT.
     *
     * This endpoint does not require an Authorization header — the
     * [AuthInterceptor] only adds the Bearer token when one is available,
     * so the request goes through clean when the user is not yet logged in.
     */
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto
}
