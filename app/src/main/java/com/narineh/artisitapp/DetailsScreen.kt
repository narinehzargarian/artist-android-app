package com.narineh.artisitapp

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.narineh.artisitapp.network.ArtistService
import com.narineh.artisitapp.network.RetrofitClient
import com.narineh.artisitapp.network.RetrofitClient.artistService
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import com.narineh.artisitapp.network.UserData
import kotlinx.coroutines.launch
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import kotlin.text.Regex

data class TabItem(
    val title: String,
    val Icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    user: UserData?,
    artistId: String,
    favorites: SnapshotStateList<String>,
    onToggleFavorite: (String) -> Unit,
    isLoggedIn: Boolean = false,
    navController: NavController,
) {
    val tabs = listOf(
        TabItem("Details", Icons.Outlined.Info),
        TabItem("Artwork", Icons.Outlined.AccountBox),
        TabItem("Similar", Icons.Outlined.PersonSearch))

    // Show only Details, Artwork to guests
    val visibleTabs = if (isLoggedIn) tabs else tabs.take(2)

//    Log.d("DetailsScreen", "Visible tabs: $isLoggedIn")

    val currentTab = remember { mutableStateOf(visibleTabs.first().title) }
    var artistDetails = remember { mutableStateOf<JsonObject?>(null) }
    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

//    Log.d("Navigator", "Artist ID: $artistId")
    LaunchedEffect(artistId) {
        // Get artist details
        val dataResponse = artistService.getArtistDetails(artistId)
        if (dataResponse.isSuccessful) {
            artistDetails.value = dataResponse.body()
//            Log.d("DetailsScreen", "Artist details: ${artistDetails.value}")
        }
    }

    Scaffold (
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(artistDetails.value?.get("name")?.jsonPrimitive?.content ?: "",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Screen"
                        )
                    }
                },
                actions = {
                    if (isLoggedIn == true) {
                        IconButton(
                            onClick = {
                                val currentlyFav = favorites.contains(artistId)
                                onToggleFavorite(artistId)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "${if (currentlyFav) "Removed from favorites" else "Added to favorites"}",
                                        duration = SnackbarDuration.Short)
                                }
                           },
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(end = 2.dp)){
                            Icon(
                                imageVector = if(favorites.contains(artistId))
                                    Icons.Filled.Star
                                else Icons.Outlined.StarBorder,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                contentDescription = "Favorite"
                            )
                        }

                    }
                },
                colors = TopAppBarDefaults.topAppBarColors (
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
            )
        }
    ) { innerPadding ->
        val selectedTab = visibleTabs.indexOfFirst { it.title == currentTab.value }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),

                contentColor = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                visibleTabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = index == selectedTab,
                        onClick = { currentTab.value = tab.title },
                        icon = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = tab.Icon,
                                    contentDescription = tab.title
                                )
                                Text(tab.title, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    )
                }
            }
            when (currentTab.value) {
                "Details" -> DetailsTab(
                    artistId = artistId,
                )
                "Artwork" -> ArtworkTab(
                    artistId = artistId,
                )

                "Similar" -> SimilarArtists(
                    artistId = artistId,
                    isLoggedIn = isLoggedIn,
                    onToggleFavorite = onToggleFavorite,
                    favorites = favorites,
                    navController = navController,
                    snackBar = snackbarHostState
                )
            }
        }
    }
}
@Composable
fun DetailsTab(
    artistId: String,
) {
    var isLoading by remember { mutableStateOf(false) }
    var artistDetails by remember { mutableStateOf<JsonObject?>(null) }

    LaunchedEffect(artistId) {
        isLoading = true
        // Get artist details
        try {
            val dataResponse = artistService.getArtistDetails(artistId)
            if (dataResponse.isSuccessful) {
                artistDetails = dataResponse.body()
                Log.d("DetailsTab", "Artist details: ${artistDetails}")
            }
            else {
                Log.e("DetailsTab", "Network error: ${dataResponse.code()}")
            }
        }
        catch (e: Exception) {
            Log.e("DetailsTab", "Network error", e)
        }
        finally {
            isLoading = false

        }
    }
    val details = artistDetails ?: JsonObject(emptyMap())
    val name = details["name"]?.jsonPrimitive?.content ?: ""
    val bio = details["biography"]?.jsonPrimitive?.content ?: ""
    val birthday = details["birthday"]?.jsonPrimitive?.content ?: ""
    val deathday = details["deathday"]?.jsonPrimitive?.content ?: ""
    val nationality = details["nationality"]?.jsonPrimitive?.content ?: ""

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if(isLoading) {
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator()
                Text("Loading…")
            }
        }
        else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ){
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = "$nationality, $birthday - $deathday",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = cleanBio(bio),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    textAlign = TextAlign.Start
                )
            }
        }
    }

}
@Composable
fun ArtworkTab (
    artistId: String,
    artistService: ArtistService = RetrofitClient.artistService
) {
    val coroutine = rememberCoroutineScope()
    var artworks by remember { mutableStateOf<List<JsonObject>>(emptyList())}
    var isLoading by remember { mutableStateOf(true) }

    // Get artist artworks
    LaunchedEffect(artistId) {
        isLoading = true
        try {
            val artworkResp = artistService.getArtistArtwork(artistId)
            if (artworkResp.isSuccessful) {
                val artistArtwork = artworkResp.body()
                    ?.get("_embedded")?.jsonObject
                    ?.get("artworks")?.jsonArray
                    ?.map { it.jsonObject } ?: emptyList()
                artworks = artistArtwork
                Log.d("ArtworkTab", "Artist details: ${artworks}")
            }
            else {
                Log.e("ArtworkTab", "Network error: ${artworkResp.code()}")
            }
        }
        catch (e: Exception) {
            Log.e("ArtworkTab", "Network error", e)
        }
        finally {
            isLoading = false
        }
    }

    if(isLoading) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularProgressIndicator()
            Text("Loading…")
        }
    }

    // No artworks
    else if (artworks.isEmpty()) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(50.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ){
            Text("No Artworks",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium)
        }
    }
    else {
        LazyColumn {
            items(artworks) { artwork ->
                val title = artwork["title"]?.jsonPrimitive?.content ?: ""
                val imgUrl = artwork["_links"]?.jsonObject
                    ?.get("thumbnail")?.jsonObject
                    ?.get("href")?.jsonPrimitive?.content ?: ""
                var showDialog = remember { mutableStateOf(false) }
                var categories = remember { mutableStateOf<List<JsonObject>>(emptyList()) }
                var isCategoryLoading = remember { mutableStateOf(true) }
                Card(
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.padding(8.dp),
                    shape = RoundedCornerShape(8.dp),

                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    )
                    ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AsyncImage(
                            // Use artwork image if exists
                            model = imgUrl,
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${artwork["title"]?.jsonPrimitive?.content ?: ""}, ${artwork["date"]?.jsonPrimitive?.content ?: ""} ",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color =MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Categories button
                        Button(
                            modifier = Modifier.padding(bottom = 8.dp),
                            onClick = {
                                isCategoryLoading.value = true
                                 showDialog.value = true
                                coroutine.launch {
                                    val response =
                                        artistService.getCategories(artwork["id"]!!.jsonPrimitive.content)
                                    if (response.isSuccessful) {
                                        val cats = response.body()
                                            ?.get("_embedded")?.jsonObject
                                            ?.get("genes")?.jsonArray
                                            ?.map { it.jsonObject } ?: emptyList()
                                        categories.value = cats
                                    }
                                    isCategoryLoading.value = false
                                }
                            },
                        ) {
                            Text("View Categories")
                        }
                    }
                }
                // Alert dialog for categories
                if (showDialog.value) {
                    if (isCategoryLoading.value) {
                        AlertDialog(
                            onDismissRequest = {showDialog.value = false},
                            title = { Text("Categories") },
                            modifier = Modifier
                                .fillMaxWidth(),
                            text = {
                                Box(modifier = Modifier
                                    .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ){
                                    Column(
                                        Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
                                    ) {
                                        CircularProgressIndicator()
                                        Text("Loading…")
                                    }
                                }
                            },
                            confirmButton = {
                                Row (
                                    Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(onClick = {showDialog.value = false }){Text("Close")}
                                }
                            }
                        )
                    }
                    // No categories available
                    else if (categories.value.isEmpty()) {
                        AlertDialog(
                            onDismissRequest = { showDialog.value = false },
                            title = { Text("Categories") },
                            text = {
                                Box(modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ){ Text("No categories available") } },
                            confirmButton = {
                                Button(onClick = {showDialog.value = false }){Text("Close")}
                            }
                        )
                    }
                    else {
                        CategoriesCarousel(
                            categories = categories.value,
                            onDismiss = { showDialog.value = false },
                            isLoading = isCategoryLoading.value
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoriesCarousel(
    categories: List<JsonObject>,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {

    val wrappedItems = remember(categories) {
        listOf(categories.last()) + categories + listOf(categories.first())
    }
    val firstRealItem = 1
    val lastRealItem = wrappedItems.size - 2

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = firstRealItem)

    // Wrap logic
    LaunchedEffect(listState) {
        snapshotFlow{ listState.firstVisibleItemIndex }
            .collect { index ->
                Log.d("CategoriesCarousel", "saw index: $index")
                when(index) {
                    // Go to  lastRealItem
                    0 -> {
                        Log.d("CategoriesCarousel", "Scrolling to $lastRealItem")
                        listState.scrollToItem(lastRealItem)
                    }
                    // Go to firstRealItem
                    wrappedItems.size - 1 -> {
                        Log.d("CategoriesCarousel", "Scrolling to $firstRealItem")
                        listState.scrollToItem(firstRealItem)
                    }
                }
            }
    }
    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(.8f),
        title = { Text("Categories") },
        text = {
            Box (modifier = Modifier
                .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                LazyRow(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.CenterHorizontally)
                ) {
                    items(wrappedItems) { item ->
                        CategoryDialog(
                            category = item,
                            modifier = Modifier
                                .width(280.dp)
                                .height(600.dp)
                        )
                    }
                }
                IconButton(
                    onClick = {
                        // Move back
                        scope.launch{
                            val idx = listState.firstVisibleItemIndex - 1
                            listState.animateScrollToItem(idx)
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous"
                    )
                }
                IconButton(
                    onClick = {
                        // Move forward
                        scope.launch{
                            val idx = listState.firstVisibleItemIndex + 1
                            listState.animateScrollToItem(idx)
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next"
                    )
                }
            }
        },
        // Close for alert dialog
        confirmButton = {
            // Close the dialog
            Button(onClick = { onDismiss() }) {
                Text("Close")
            }
        }
    )
}

@Composable
fun CategoryDialog(
    category: JsonObject,
    modifier: Modifier = Modifier
){
    // date, description, image for genes
    val name = category["name"]?.jsonPrimitive?.content ?: ""
    val description = category["description"]?.jsonPrimitive?.content ?: ""
    val img = category["_links"]?.jsonObject
        ?.get("thumbnail")?.jsonObject
        ?.get("href")?.jsonPrimitive?.content ?: ""

    Card( modifier = modifier
        .padding(4.dp),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ){
            AsyncImage(
                model = img,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(8.dp),
                alignment = Alignment.Center
            )
            Text(
                text = name,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .height(200.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Description(
                    text = cleanBio(description),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }

            }
    }
}

@Composable
fun Description(
    text: String,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val url = "https://www.artsy.net"
    val annotatedText = remember(text) {
        buildAnnotatedString {
            var last = 0
            // match[label](relative path)
            val regex = Regex("""\[(.+?)\]\(([^)]+)\)""")
            for (match in regex.findAll(text)) {
                append(text.substring(last, match.range.first))
                val (label, relUrl) = match.destructured
                val fullUrl = if (relUrl.startsWith("http")) relUrl
                else url + if (relUrl.startsWith("/")) relUrl else "/$relUrl"
                pushStringAnnotation(tag = "URL", annotation = fullUrl)

                withStyle(style = SpanStyle(
                    textDecoration = TextDecoration.None
                )){
                    append(label)
                }
                pop() // pop annotation
                last = match.range.last + 1
            }
            append(text.substring(last))
        }
    }

    ClickableText(
        text = annotatedText,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurface),
        onClick = { offset ->
            annotatedText
                .getStringAnnotations(tag = "URL", offset, offset)
                .firstOrNull()
                ?.let { uriHandler.openUri(it.item) }
        }
    )
}

@Composable
fun SimilarArtists(
    artistId: String,
    isLoggedIn: Boolean,
    onToggleFavorite: (String) -> Unit,
    favorites: SnapshotStateList<String>,
    snackBar: SnackbarHostState,
    navController: NavController
) {
    var similarArtists by remember { mutableStateOf<JsonObject?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Get similar artists
    LaunchedEffect(artistId) {
        isLoading = true
        try {
            val similarResponse = artistService.getSimilarArtists(artistId)
            if (similarResponse.isSuccessful) {
                similarArtists = similarResponse.body()
            } else {
                Log.e("SimilarArtists", "Network error: ${similarResponse.code()}")
            }
        } catch (e: Exception) {
            Log.e("SimilarArtists", "Network error", e)
        } finally {
            isLoading = false
        }
    }
    val artists: List<JsonObject> = similarArtists
        ?.get("_embedded")
        ?.jsonObject
        ?.get("artists")
        ?.jsonArray?.map { it.jsonObject } ?: emptyList()
//    var navController = rememberNavController()
    val scope = rememberCoroutineScope()
    if (isLoading) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            CircularProgressIndicator()
            Text("Loading...")
        }
    } else if (artists.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(50.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No Similar Artists",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
    else {
        LazyColumn {
            items(artists) { artist ->
                val name = artist["name"]?.jsonPrimitive?.content.orEmpty()
                val imgUrl = artist["_links"]?.jsonObject
                    ?.get("thumbnail")?.jsonObject
                    ?.get("href")?.jsonPrimitive?.content.orEmpty()
                val id = artist["id"]?.jsonPrimitive?.content.orEmpty()

                val imgModel: Any = if (imgUrl.isEmpty()) {
                    R.drawable.artsy_logo
                } else {
                    imgUrl
                }
    //            Log.d("SimilarArtists", "Artist img: $imgUrl")
                Card(
                    onClick = { navController.navigate("details/${id}") },
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
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
                                text = name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                contentDescription = "View Artist Details"
                            )
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
                                        val currentlyFav = favorites.contains(id)
                                        onToggleFavorite(id)
                                        scope.launch {
                                            snackBar.showSnackbar(
                                                "${if (currentlyFav) "Removed from favorites" else "Added to favorites"}",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector =
                                            if (favorites.contains(id)) Icons.Filled.Star
                                            else Icons.Outlined.StarBorder,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        contentDescription = "Favorite"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
// Remove the unwanted characters from bio
fun cleanBio(bio: String): String {
    return bio
        .replace("_", "") // Remove markdown underscores
        .replace(Regex("-\\s+"), "") // Remove hyphens from end-of-line
        .replace(Regex("[^\\S\\r\\n]+"), " ") // Extra spaces
        .replace('\u0096', '\u2013')
        .lineSequence()
        .joinToString("\n") { it.trimEnd() }
}