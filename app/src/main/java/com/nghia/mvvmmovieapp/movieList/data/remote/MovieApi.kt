package com.nghia.mvvmmovieapp.movieList.data.remote

import com.nghia.mvvmmovieapp.movieList.data.remote.respond.MovieDto
import com.nghia.mvvmmovieapp.movieList.data.remote.respond.MovieListDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApi {

    @GET("movie/{category}")
    suspend fun getMovieList(
        @Path("category") category: String,
        @Query("page") page: Int,
        @Query("api_key") apiKey: String = API_KEY
    ): MovieListDto

    @GET("movie/{id}/similar")
    suspend fun getSimilarMovieList(
        @Path("id") id: Int,
        @Query("page") page: Int,
        @Query("api_key") apiKey: String = API_KEY
    ): MovieListDto

    companion object{
        const val BASE_URL = "https://api.themoviedb.org/3/"
        const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"
        const val API_KEY = "fefa8ffb1fac32c0da334bea64d47d45"
    }
}