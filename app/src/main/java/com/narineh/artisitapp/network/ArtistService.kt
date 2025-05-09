package com.narineh.artisitapp.network

import kotlinx.serialization.json.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ArtistService {
    @GET("api/search")
    suspend fun getArtists(@Query("name") artistName: String): Response<JsonObject>

    @GET("api/artist")
    suspend fun getArtistDetails(@Query("id") id: String): Response<JsonObject>

    @GET("api/artworks")
    suspend fun getArtistArtwork(@Query("artist_id") id: String): Response<JsonObject>

    @GET("api/genes")
    suspend fun getCategories(@Query("artwork_id") id: String): Response<JsonObject>

    @GET("api/similar_artists")
    suspend fun getSimilarArtists(@Query("artist_id") id: String): Response<JsonObject>
}