package com.narineh.artisitapp.network

import com.narineh.artisitapp.model.FavoriteArtists
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body

@Serializable
data class FavoriteRequest(
    val artistID: String
)

interface FavoriteService {
    @GET("api/favorites/get")
    suspend fun getFavorites(): Response<FavoriteArtists>

    @POST("api/favorites/add")
    suspend fun addFavorite(@Body request: FavoriteRequest): Response<JsonObject>

    @POST("api/favorites/remove")
    suspend fun removeFavorite(@Body request: FavoriteRequest): Response<JsonObject>
}