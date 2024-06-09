package com.nghia.mvvmmovieapp.details.presentation

import com.nghia.mvvmmovieapp.movieList.domain.model.Movie

data class DetailsState(
    val isLoading: Boolean = true,
    val movie: Movie? = null,
    val similarMovieList: MutableList<Movie>  = arrayListOf()
)
