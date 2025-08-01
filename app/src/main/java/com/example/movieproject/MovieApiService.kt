package com.example.movieproject

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApiService {

    @GET("discover/movie")
    fun getMovies(
        @Query("api_key") apiKey: String,
        @Query("primary_release_date.lte") releaseDate: String,
        @Query("sort_by") sortBy: String,
        @Query("page") page: Int
    ): Call<MovieResponse>

    @GET("movie/{movieId}")
    fun getMovieDetails(
        @Path("movieId") movieId: Int,  // Dynamic path parameter
        @Query("api_key") apiKey: String // Query parameter for API key
    ): Call<MovieDetailResponse>
}