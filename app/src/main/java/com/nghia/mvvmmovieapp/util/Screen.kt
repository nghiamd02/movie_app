package com.nghia.mvvmmovieapp.util

sealed class Screen(val rout: String) {
    data object Home: Screen("main")
    data object PopularMovieList: Screen("popularMovie")
    data object UpcomingMovieList: Screen("upcomingMovie")
    data object Details: Screen("details")
}