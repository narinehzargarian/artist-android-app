package com.narineh.artisitapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.narineh.artisitapp.model.HomeFavoriteItem
import java.time.Duration
import java.time.Instant
import kotlin.math.abs

@Composable
fun FavoriteListItem(
    favorite: HomeFavoriteItem,
    navController: NavController,
) {
    val artistID = favorite.artistID
    val dateAdded = getRelativeTime(favorite.dateAdded)
    val name = favorite.name
    val nationality = favorite.nationality
    val birthday = favorite.birthday
    val deliminator = if (nationality.isNotEmpty() && birthday.isNotEmpty()) ", " else ""
    val subtitle = "$nationality$deliminator $birthday"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("details/${artistID}")}
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = name, style = MaterialTheme.typography.titleMedium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
        }

        Text(text = dateAdded,
            modifier = Modifier
                .padding(end = 4.dp)
                .align(Alignment.CenterVertically),
            style = MaterialTheme.typography.bodySmall)

        IconButton(
            onClick = {navController.navigate("details/$artistID")},
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View Artist Details"
            )

        }
    }

}

fun getRelativeTime(addedTime: String): String {
    val now = Instant.now()
    val added = Instant.parse(addedTime)
    val seconds = Duration.between(added, now).seconds

    return when {
        seconds < 0 -> "${abs(seconds)} seconds ago"
        seconds < 60 -> "$seconds seconds ago"
        seconds < 3600 -> "${seconds / 60} minutes ago"
        else -> "${seconds / 3600} hours ago"
    }
}


