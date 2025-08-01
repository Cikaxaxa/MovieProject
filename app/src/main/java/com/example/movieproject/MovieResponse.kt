package com.example.movieproject

data class MovieResponse(
    val page: Int,
    val results: List<MovieItem>
)