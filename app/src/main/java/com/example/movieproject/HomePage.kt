package com.example.movieproject

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.example.movieproject.ui.theme.MovieProjectTheme
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class HomePageActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val releaseDate = ""

        setContent {
            MovieProjectTheme {
                MovieScreen(releaseDate = releaseDate)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieScreen(releaseDate: String) {
    val apiKey = "328c283cd27bd1877d9080ccb1604c91"
    var page by remember { mutableIntStateOf(1) }
    var movies by remember { mutableStateOf<List<MovieItem>>(emptyList()) }
    var filteredMovies by remember { mutableStateOf<List<MovieItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    var selectedRatings by remember { mutableStateOf(setOf<Float>()) }
    var sortOption by remember { mutableStateOf("primary_release_date.desc") }

    val movieRepository = MovieRepository()

    fun roundToNearestPointTwo(value: Float): Float {
        return (value / 0.2f).roundToInt() * 0.2f
    }

    fun loadMovies(page: Int, sortBy: String, ratings: Set<Float>) {
        movieRepository.getMovies(apiKey, releaseDate, sortBy, page) { movieResponse ->
            if (movieResponse != null) {
                var newMovies = movieResponse.results

                if (ratings.isNotEmpty()) {
                    val roundedRatings = ratings.map { roundToNearestPointTwo(it) }
                    newMovies = movieResponse.results.filter { movie ->
                        val roundedMovieRating = roundToNearestPointTwo(movie.popularity)
                        roundedRatings.contains(roundedMovieRating)
                    }
                }

                // Update movies and filteredMovies
                movies = movies + newMovies
                filteredMovies = movies
                isLoading = false // Set isLoading to false after data is loaded
            } else {
                Log.e("MoviesAPI", "Failed to load movies: No response")
                isLoading = false // Set isLoading to false if the API call fails
            }
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true // Set loading to true when page starts
        loadMovies(page, sortOption, selectedRatings) // Start the API call
    }

    fun filterMovies() {
        filteredMovies = when (sortOption) {
            "original_title.asc" -> {
                movies.sortedBy { it.title }
            }
            "original_title.desc" -> {
                movies.sortedByDescending { it.title }
            }
            else -> {
                movies
            }
        }

        if (selectedRatings.isNotEmpty()) {
            val roundedRatings = selectedRatings.map { roundToNearestPointTwo(it) }
            filteredMovies = filteredMovies.filter { movie ->
                val roundedMovieRating = roundToNearestPointTwo(movie.popularity)
                roundedRatings.contains(roundedMovieRating)
            }
        }
    }

    LaunchedEffect(movies, selectedRatings, sortOption) {
        filterMovies()
    }

    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull() }
            .collect {
                val lastVisibleItem = it
                if (lastVisibleItem != null && !isLoading && lastVisibleItem.index == filteredMovies.size - 1) {
                    isLoading = true
                    delay(1000)
                    page += 1
                    loadMovies(page, sortOption, selectedRatings)
                    isLoading = false
                }
            }
    }

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            if (!isLoading) {
                isLoading = true
                movies = emptyList()
                selectedRatings = emptySet()
                sortOption = "primary_release_date.desc"

                CoroutineScope(Dispatchers.Main).launch {
                    delay(1000)
                    loadMovies(1, sortOption, selectedRatings)
                    isLoading = false
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Movies") },
                    actions = {
                        IconButton(onClick = { showDialog = true }) {
                            Icon(imageVector = Icons.Filled.Menu, contentDescription = "Sort Movies")
                        }
                    }
                )
            },
            content = { paddingValues ->

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filteredMovies) { movie ->
                        val context = LocalContext.current
                        MovieCard(movie = movie, context = context)
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .wrapContentSize(Alignment.Center)
                            .zIndex(1f)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(100.dp)
                                .align(Alignment.Center),
                            strokeWidth = 6.dp
                        )
                    }
                }

                if (filteredMovies.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                            .background(Color(0xFFF5F5F5)) // Light grey background for better contrast
                            .padding(16.dp) // Adding some padding around the text
                            .clip(RoundedCornerShape(12.dp)) // Rounded corners to make it look more modern
                    ) {
                        Text(
                            text = "No movies available",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color(0xFF3E3E3E), // Darker text color for better readability
                                fontWeight = FontWeight.Bold // Making the text bold
                            ),
                            modifier = Modifier
                                .align(Alignment.Center) // Ensuring the text is centered
                                .padding(16.dp) // Adding padding to avoid text from touching the edges
                        )
                    }
                }

                if (showDialog) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.9f))
                            .zIndex(0f)
                    )
                }

                if (showDialog) {
                    Dialog(onDismissRequest = { showDialog = false }) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Filter Movies",
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                    )

                                    IconButton(onClick = { showDialog = false }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Close")
                                    }
                                }

                                Text(text = "Select Rating (0.0 to 1.0):", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(8.dp))

                                Column {
                                    val ratings = (0..5).map { it * 0.2f }
                                    ratings.forEach { rating ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Start
                                        ) {
                                            Checkbox(
                                                checked = selectedRatings.contains(rating),
                                                onCheckedChange = { isChecked ->
                                                    selectedRatings = if (isChecked) {
                                                        selectedRatings + rating
                                                    } else {
                                                        selectedRatings - rating
                                                    }
                                                }
                                            )
                                            Text(text = "%.1f".format(rating), modifier = Modifier.padding(start = 8.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(text = "Sort By:", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = sortOption == "original_title.asc",
                                            onCheckedChange = { isChecked ->
                                                sortOption = if (isChecked) {
                                                    "original_title.asc"
                                                } else {
                                                    ""
                                                }
                                            }
                                        )
                                        Text(text = "Alphabetical (A to Z)", modifier = Modifier.padding(start = 8.dp))
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = sortOption == "original_title.desc",
                                            onCheckedChange = { isChecked ->
                                                sortOption = if (isChecked) {
                                                    "original_title.desc"
                                                } else {
                                                    ""
                                                }
                                            }
                                        )
                                        Text(text = "Alphabetical (Z to A)", modifier = Modifier.padding(start = 8.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(onClick = {
                                        selectedRatings = emptySet()
                                        sortOption = "primary_release_date.desc"
                                        showDialog = false
                                    }) {
                                        Text(text = "Clear Filters")
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(onClick = {
                                        showDialog = false
                                        filterMovies()
                                    }) {
                                        Text(text = "Apply")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun MovieCard(movie: MovieItem, context: Context) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        val posterUrl = movie.poster_path?.let { "https://image.tmdb.org/t/p/w500$it" }
            ?: "android.resource://com.example.movieproject/${R.drawable.invalid}"

        val isClicked by remember { mutableStateOf(false) }

        Image(
            painter = rememberAsyncImagePainter(model = posterUrl),
            contentDescription = "Movie Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    val intent = Intent(context, MovieDetailActivity::class.java)
                    intent.putExtra("movieId", movie.id)
                    context.startActivity(intent)
                }
                .border(2.dp, Color.Gray, RoundedCornerShape(16.dp))
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .padding(4.dp)
                .graphicsLayer {
                    val scale = if (isClicked) 1.1f else 1.0f
                    this.scaleX = scale
                    this.scaleY = scale
                },
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = movie.title, style = MaterialTheme.typography.headlineMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.2f), shape = MaterialTheme.shapes.medium)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .wrapContentWidth()
            ) {
                Text(
                    text = "Rating : ${movie.popularity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black
                )
            }

            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.2f), shape = MaterialTheme.shapes.medium)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .wrapContentWidth()
            ) {
                Text(
                    text = "Release Date : ${movie.release_date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomePage() {
    MovieProjectTheme {
        MovieScreen(releaseDate = "2025-07-23")
    }
}
