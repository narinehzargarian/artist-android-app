package com.narineh.artisitapp.network

import com.narineh.artisitapp.model.FavoriteItem
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {
    // Check login status
    @GET("api/auth/status") // Check login status
    suspend fun checkLoginStatus(): Response<LoginStatus>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginStatus>

    @POST("api/users") // Register
    suspend fun register(@Body request: RegisterRequest): Response<LoginStatus>

    @DELETE("api/users/me")
    suspend fun deleteAccount(): Response<JsonObject>
}

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val fullname: String,
    val email: String,
    val password: String
)

@Serializable
data class LoginStatus(
    val token: String,
    val message: String,
    val user: UserData
)

@Serializable
data class UserData(
    val fullname: String,
    val email: String,
    val profileImageUrl: String? = null,
    val favorites: List<FavoriteItem> ?= emptyList()
)