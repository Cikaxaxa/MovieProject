package com.example.movieproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("DEPRECATION")
class MovieDetailActivity : ComponentActivity() {
    private val apiKey = "328c283cd27bd1877d9080ccb1604c91"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val movieId = intent.getIntExtra("movieId", -1)

        if (movieId == -1) {
            Toast.makeText(this, "Movie ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            MovieDetailScreen(
                movieId = movieId,
                apiKey = apiKey,
                onBackPressed = { onBackPressed() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(movieId: Int, apiKey: String, onBackPressed: () -> Unit) {
    var movieDetail by remember { mutableStateOf<MovieDetailResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(movieId) {
        getMovieDetails(movieId, apiKey) { detail ->
            movieDetail = detail
            isLoading = false
        }
    }

    if (isLoading) {
        Text(
            text = "Loading movie details...",
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        )
    } else {
        movieDetail?.let { detail ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp)
                ) {
                    TopAppBar(
                        title = { Text(text = "Movie Details") },
                        navigationIcon = {
                            IconButton(onClick = { onBackPressed() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.Black
                                )
                            }
                        },
                    )

                    MovieDetailView(movieDetail = detail)
                }

            }
        } ?: run {
            Text(text = "Failed to load movie details.", color = Color.Red)
        }
    }
}

private fun getMovieDetails(movieId: Int, apiKey: String, onResult: (MovieDetailResponse?) -> Unit) {
    RetrofitInstance.api.getMovieDetails(movieId, apiKey).enqueue(object : Callback<MovieDetailResponse> {
        override fun onResponse(call: Call<MovieDetailResponse>, response: Response<MovieDetailResponse>) {
            if (response.isSuccessful) {
                onResult(response.body())
            } else {
                onResult(null)
            }
        }

        override fun onFailure(call: Call<MovieDetailResponse>, t: Throwable) {
            onResult(null)
            Log.e("MovieDetail", "API call failed: ${t.message}")
        }
    })
}

@Composable
fun MovieDetailView(movieDetail: MovieDetailResponse) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = movieDetail.title,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            ),
            color = Color.Black,
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        val backdropUrl = when {
            !movieDetail.poster_path.isNullOrEmpty() -> {
                "https://image.tmdb.org/t/p/w500${movieDetail.poster_path}"
            }
            !movieDetail.backdrop_path.isNullOrEmpty() -> {
                "https://image.tmdb.org/t/p/w500${movieDetail.backdrop_path}"
            }
            else -> {
                "android.resource://com.example.movieproject/${R.drawable.invalid}" // Replace with your drawable resource
            }
        }

        Log.e("backdropUrl", "Error: $backdropUrl")

        Image(
            painter = rememberAsyncImagePainter(backdropUrl),
            contentDescription = "Backdrop Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(8.dp)
                .border(2.dp, Color.Gray, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            movieDetail.genres.forEach { genre ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(Color.Gray.copy(alpha = 0.2f), shape = MaterialTheme.shapes.medium)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = genre.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = "Synopsis",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = movieDetail.overview.ifEmpty {
                    "No overview available"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Language",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(Color.Gray.copy(alpha = 0.2f), shape = MaterialTheme.shapes.medium)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = movieDetail.original_language,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Duration",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(Color.Gray.copy(alpha = 0.2f), shape = MaterialTheme.shapes.medium)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (movieDetail.runtime > 0) {
                            "${movieDetail.runtime} min"
                        } else {
                            "Runtime not available"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "Release Date",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = movieDetail.release_date.ifEmpty {
                        "No overview available"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            FloatingActionButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW,
                        "https://www.cathaycineplexes.com.sg/".toUri())
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(16.dp),
                containerColor = Color(0xFF81C784)
            ) {
                Text(
                    text = "Book Now",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMovieDetailScreen() {
    MovieDetailScreen(
        movieId = 12312,
        apiKey = "123123123",
        onBackPressed = { }
    )
}
