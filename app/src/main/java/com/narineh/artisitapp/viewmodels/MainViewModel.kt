package com.narineh.artisitapp.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.narineh.artisitapp.model.HomeFavoriteItem
import com.narineh.artisitapp.network.FavoriteRequest
import com.narineh.artisitapp.network.RetrofitClient
import com.narineh.artisitapp.network.UserData
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive

class MainViewModel: ViewModel(){
    var activeUser by mutableStateOf<UserData?>(null)
        private set
    var favorites = mutableStateListOf<String>()
        private set
    var favoriteDetails = mutableStateListOf<HomeFavoriteItem>()
        private set

    // Check login status
    fun checkLoginStatus() {
        viewModelScope.launch {
            val resp = RetrofitClient.authService.checkLoginStatus()
            if (resp.isSuccessful) {
                activeUser = resp.body()!!.user
                favorites.clear()
                favorites += activeUser?.favorites?.map{ it.artistID }.orEmpty()
                loadFavoriteDetails()
            }
            else {
                clearSession()
            }
        }
    }

    fun onLoginSuccess(user: UserData) {
        activeUser = user
        favorites.clear()
        favorites += activeUser?.favorites?.map{ it.artistID }.orEmpty()
        loadFavoriteDetails()
    }

    // Toggle favorites
    fun toggleFavorite(id: String) {
        val isFavorite = favorites.contains(id)
        if (isFavorite) {
            favorites.remove(id)
        }
        else {
            favorites.add(id)
        }
        viewModelScope.launch {
            if (isFavorite) {
                val resp = RetrofitClient.favoriteService.removeFavorite(FavoriteRequest(id))
                if (!resp.isSuccessful) {
                    // Roll back the remove
                    favorites.add(id)
                }
            }
            else {
                val resp = RetrofitClient.favoriteService.addFavorite(FavoriteRequest(id))
                if (!resp.isSuccessful) {
                    // Roll back the add
                    favorites.remove(id)
                }
            }
            val newFavorites = RetrofitClient.favoriteService.getFavorites()
            if (newFavorites.isSuccessful) {
                activeUser = activeUser?.copy(favorites = newFavorites.body()!!.favorites)
                favorites.clear()
                favorites += activeUser?.favorites?.map{ it.artistID }.orEmpty()

                // reload favorites details
                loadFavoriteDetails()
            }
        }
    }

    // Clear session data
    private fun clearSession() {
        RetrofitClient.clearJar()
        activeUser = null
        favorites.clear()
        favoriteDetails.clear()
    }

    fun logout() {
        clearSession()
    }

    fun loadFavoriteDetails() {
        favoriteDetails.clear()

        viewModelScope.launch {
            activeUser?.favorites?.forEach { favorite ->
                val resp = RetrofitClient.artistService.getArtistDetails(favorite.artistID)
                if (resp.isSuccessful) {
                    val details = resp.body()!!
                    val name = details.get("name")?.jsonPrimitive?.content.orEmpty()
                    val nationality = details.get("nationality")?.jsonPrimitive?.content.orEmpty()
                    val birthday = details.get("birthday")?.jsonPrimitive?.content.orEmpty()
                    favoriteDetails.add(
                        HomeFavoriteItem(
                            artistID = favorite.artistID,
                            name = name,
                            nationality = nationality,
                            birthday = birthday,
                            dateAdded = favorite.dateAdded
                        )
                    )
                }
                else {
                    Log.e("MainViewModel", "Error loading favorite details")
                }
            }
        }
    }

}