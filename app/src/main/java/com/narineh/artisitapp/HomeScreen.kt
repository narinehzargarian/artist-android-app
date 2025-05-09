package com.narineh.artisitapp

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.narineh.artisitapp.network.UserData
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import com.narineh.artisitapp.model.HomeFavoriteItem
import com.narineh.artisitapp.network.RetrofitClient
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: UserData?,
    favorites: List<HomeFavoriteItem>,
    isloggedIn: Boolean,
    navController: NavController,
    onLogout: () -> Unit
) {
    var dropDown by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarState = remember { SnackbarHostState() }

    Scaffold (
        snackbarHost = { SnackbarHost(hostState = snackbarState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Artist Search")
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("search")
                    }){
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                    IconButton(onClick = {
                        if (!isloggedIn) navController.navigate("login")}
                    ){
                        if (!isloggedIn) {
                            Icon(
                                imageVector = Icons.Default.PersonOutline,
                                contentDescription = "Login"
                            )
                        }
                        else {
                            AsyncImage(
                                model = user?.profileImageUrl,
                                contentDescription = "Gravatar Picture",
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        dropDown = !dropDown
                                    }
                            )
                        }
                    }
                    DropdownMenu(
                        onDismissRequest = { dropDown = false },
                        expanded = dropDown
                    ) {
                        // Logout
                        DropdownMenuItem(
                            text = { Text("Log Out") },
                            onClick = {
                                dropDown = false
                                scope.launch {
                                    snackbarState.showSnackbar("Logged out successfully",  duration = SnackbarDuration.Short)
                                }
                                onLogout()
                            }
                        )
                        // Delete account
                        DropdownMenuItem(
                            text = { Text("Delete Account", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                dropDown = false
                                scope.launch {
                                    val res = RetrofitClient.authService.deleteAccount()
                                    if (res.isSuccessful) {
                                        snackbarState.showSnackbar("Deleted user successfully", duration = SnackbarDuration.Short)
                                        onLogout()
                                    }
                                    else {
                                        Log.e("HomeScreen", "Failed to delete user: ${res.errorBody()?.string()}")
                                    }
                                }
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { InnerPadding ->
        HomeScreenContent(
            modifier = Modifier.padding(InnerPadding),
            user = user,
            isloggedIn = isloggedIn,
            navController = navController,
            favorites = favorites
        )
    }
}
@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    user: UserData?,
    favorites: List<HomeFavoriteItem>,
    isloggedIn: Boolean,
    navController: NavController
) {
    val context = LocalContext.current
    val today = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM, yyyy"))
    }

    // Layout
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = today,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(
            modifier = Modifier.height(16.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .wrapContentHeight()
                    .background(color =  MaterialTheme.colorScheme.surfaceContainer),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Favorites",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp),
                )
            }

        }
        if (isloggedIn) {
            if (favorites.isEmpty()) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ){
                    Text("No Favorites",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
            else {
                LazyColumn (modifier = Modifier
                    .fillMaxWidth()
                ){
                   items(favorites) { favorite ->
                       FavoriteListItem(favorite = favorite, navController = navController)
                   }
                }
            }
        }
        else {
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = {navController.navigate("login")},
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Log in to see favorites")
            }
        }
        // Push the following content to the bottom
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "Powered by Artsy",
            modifier = Modifier.align(Alignment.CenterHorizontally)
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.artsy.net/"))
                    context.startActivity(intent)
                }
            ,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                fontStyle = FontStyle.Italic
            ),
        )
    }

}