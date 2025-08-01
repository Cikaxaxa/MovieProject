package com.example.movieproject

data class MovieItem(
    val adult: Boolean,
    val backdrop_path: String?,
    val genre_ids: List<Int>,
    val id: Int,
    val original_language: String,
    val original_title: String,
    val overview: String,
    val popularity: Float,
    val poster_path: String?,
    val release_date: String,
    val title: String,
    val video: Boolean,
    val vote_average: Double,
    val vote_count: Int
)

data class MovieDetailResponse(
    val title: String,
    val overview: String,
    val release_date: String,
    val original_language: String,
    val poster_path: String,
    val backdrop_path: String,
    val genres: List<Genre>,
    val budget: Int,
    val revenue: Long,
    val runtime: Int,
    val vote_average: Double,
    val tagline: String,
    val homepage: String,
    val production_companies: List<ProductionCompany>
)

data class Genre(
    val id: Int,
    val name: String
)

data class ProductionCompany(
    val id: Int,
    val name: String,
    val logo_path: String?
)


