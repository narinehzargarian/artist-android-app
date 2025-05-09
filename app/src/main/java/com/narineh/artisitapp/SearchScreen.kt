package com.narineh.artisitapp

import android.graphics.drawable.Icon
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import com.narineh.artisitapp.network.RetrofitClient
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.narineh.artisitapp.network.UserData
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.unit.sp



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    searchResults: List<JsonObject> = emptyList()
) {
    var active by rememberSaveable { mutableStateOf(false) }
    SearchBar(
        modifier = modifier,
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                expanded = active,
                onExpandedChange = { active = it},
                placeholder = { Text("Search artists...",
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp)
                )},
                leadingIcon = { Icon(Icons.Filled.Search,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    contentDescription = "Search")},
                trailingIcon = {
                    IconButton(onClick = onClearClick) {
                        Icon(Icons.Filled.Clear,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            contentDescription = "Clear")
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        },
        expanded = active,
        onExpandedChange = { active = it},
        colors = SearchBarDefaults.colors(
            containerColor = Color.Transparent,
            dividerColor = Color.Transparent,
        ),
    ) {}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    isLoggedIn: Boolean,
    favorites: SnapshotStateList<String>,
    onToggleFavorite: (String) -> Unit,
    user: UserData?
) {
    var query by rememberSaveable { mutableStateOf("") }
    var result by remember { mutableStateOf<List<JsonObject>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(query) { // Re-fetch the query
        if (query.length >= 3) {
            coroutineScope.launch {
                result = getArtists(query)
            }
        }
        else {
            result = emptyList()
        }
    }
    Scaffold (
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title =  {

                    ArtistSearchBar(
                    query = query,
                    onQueryChange = { artist ->
                        query = artist
                        // Call API only if the query is at least 3 characters long
                        if (query.length >= 3) {
                            coroutineScope.launch {
                                result = getArtists(query)
                            }
                        } else {
                            result = emptyList()
                        }
                    },
                    onClearClick = {
                        result = emptyList()
                        query = ""
                        navController.navigate("home")
                    },
                    onSearch = {
                        coroutineScope.launch { // Call API on search
                            result = getArtists(query)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) },
                colors = TopAppBarDefaults.topAppBarColors (
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        }
    ){ innerPadding ->
        //Show the list of artists if there are any
        if (result.isEmpty() && query.length > 3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(innerPadding)
                    .height(50.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("No Results Found",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
            ) {
                items(result) { artist ->
                    val id = artist["_links"]?.jsonObject
                        ?.get("self")?.jsonObject
                        ?.get("href")?.jsonPrimitive?.content?.substringAfterLast("/") ?: ""
                    val isFav = favorites.contains(id)
                    ArtistCard(
                        artist = artist,
                        isLoggedIn = isLoggedIn,
                        isFav = isFav, // Does the artist exist in the list?
                        onToggleFavorite = {
                            onToggleFavorite(id)
                            coroutineScope.launch {

                            }
                        },
                        navController = navController,
                        snackBar = snackbarHostState
                    )
                }
            }
        }
    }
}

@Composable
fun ArtistCard(
    artist: JsonObject,
    isLoggedIn: Boolean,
    isFav: Boolean,
    onToggleFavorite: () -> Unit,
    navController: NavController,
    snackBar: SnackbarHostState
) {

    // fetch the image url from the artist object
    var imgUrl = artist["_links"]?.jsonObject
        ?.get("thumbnail")?.jsonObject
        ?.get("href")?.jsonPrimitive?.content ?: ""
    val id = artist["_links"]?.jsonObject
        ?.get("self")?.jsonObject
        ?.get("href")?.jsonPrimitive?.content?.substringAfterLast("/") ?: ""

    val imgModel : Any = if (imgUrl?.contains("missing_image") == true) {
        R.drawable.artsy_logo
    } else {
        imgUrl
    }
    val scope = rememberCoroutineScope()

    Card(
        onClick = { navController.navigate("details/${id}") },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = imgModel,
                contentDescription = "Artist Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                    .height(30.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,

            ) {
                Text(
                    text = artist["title"]?.jsonPrimitive?.content ?: "",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 8.dp)
                )
                IconButton(
                    onClick = {
                        navController.navigate("details/${id}")
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        contentDescription = "View Artist Details"
                    )
                }
            }
            if (isLoggedIn) {
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 2.dp
                ) {
                    IconButton(
                        onClick = {

                            onToggleFavorite()
                            scope.launch {
                                snackBar.showSnackbar(
                                    "${if (isFav) "Removed from favorites" else "Added to favorites"}",
                                    duration = SnackbarDuration.Short)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            imageVector =
                            if (!isFav) {
                                Icons.Outlined.StarBorder
                            } else {
                                Icons.Filled.Star
                            },
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            contentDescription = "Favorite"
                        )
                    }
                }
            }
        }
    }
}

suspend fun getArtists(query: String): List<JsonObject> {
    try {
        val response = RetrofitClient.artistService.getArtists(query)
        if (response.isSuccessful) {
            val body: JsonObject? = response.body()
            Log.d("getArtists", "Response body: $body")
            // _embedded field
            val embedded = body?.get("_embedded")?.jsonObject
            Log.d("getArtists", "Embedded: $embedded")
            // results field
            val results = embedded?.get("results")?.jsonArray
            Log.d("getArtists", "Results: $results")
            return results?.map { it.jsonObject } ?: emptyList()
        }
        else return emptyList()
    }
    catch (e: Exception) {
        Log.e("getArtists", "Error: ${e.localizedMessage}")
        return emptyList()
    }
}