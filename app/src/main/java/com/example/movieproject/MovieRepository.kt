package com.example.movieproject

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import android.util.Log

class MovieRepository {
    fun getMovies(apiKey: String, releaseDate: String, sortBy: String, page: Int, callback: (MovieResponse?) -> Unit) {
        RetrofitInstance.api.getMovies(apiKey, releaseDate, sortBy, page).enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                // Log response code and body
                Log.d("MovieRepository", "Response Code: ${response.code()}")
                if (response.isSuccessful) {
                    // Log success and body
                    Log.d("MovieRepository", "Response Body: ${response.body()}")
                    callback(response.body())
                } else {
                    // Log failure response
                    Log.e("MovieRepository", "Error: ${response.message()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                // Log failure message
                Log.e("MovieRepository", "Failure: ${t.message}")
                callback(null)
            }
        })
    }
}

