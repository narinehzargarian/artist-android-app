package com.narineh.artisitapp.model

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteItem (
    val artistID: String,
    val dateAdded: String,
)

@Serializable
data class FavoriteArtists(
    val favorites: List<FavoriteItem>
)

@Serializable
data class HomeFavoriteItem (
    val artistID: String,
    val name: String,
    val nationality: String,
    val birthday: String,
    val dateAdded: String,
)