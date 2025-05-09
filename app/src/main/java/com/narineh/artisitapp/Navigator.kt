package com.narineh.artisitapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.narineh.artisitapp.viewmodels.MainViewModel

@Composable
fun Navigator(viewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    val activeUser = viewModel.activeUser
    val favorites = viewModel.favorites
    val favoriteDetails = viewModel.favoriteDetails
    val isLoggedIn = activeUser != null

    LaunchedEffect(Unit) {
        viewModel.checkLoginStatus()
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                user = activeUser,
                isloggedIn = isLoggedIn,
                favorites = favoriteDetails,
                navController = navController,
                onLogout = {
                    viewModel.logout()
                })
        }
        composable("search") {
            SearchScreen(
                navController = navController,
                isLoggedIn = isLoggedIn,
                favorites = favorites,
                onToggleFavorite = {
                    viewModel.toggleFavorite(it)
                },
                user = activeUser
            )
        }
        composable("details/{id}") { back ->
            val id = back.arguments?.getString("id") ?: ""
            DetailsScreen(
                user = activeUser,
                artistId = id,
                favorites = favorites,
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                navController = navController,
                isLoggedIn = isLoggedIn
            )
        }
        composable("login") {
            LoginScreen(onLogin = { user ->
                viewModel.onLoginSuccess(user)
                navController.navigate("home")
            }, navController = navController)
        }
        composable("register") {
            RegisterScreen(
                onRegister = { user ->
                    viewModel.onLoginSuccess(user)
                    navController.navigate("home")
                }, navController = navController
            )
        }
    }
}


