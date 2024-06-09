package com.nghia.mvvmmovieapp.details.presentation

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nghia.mvvmmovieapp.movieList.domain.model.Movie
import com.nghia.mvvmmovieapp.movieList.domain.repository.MovieListRepository
import com.nghia.mvvmmovieapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val movieListRepository: MovieListRepository,
    savedStateHandle: SavedStateHandle
): ViewModel(){

    private val movieId = savedStateHandle.get<Int>("movieId")
    private var _detailsState = MutableStateFlow(DetailsState())
    val detailsState = _detailsState.asStateFlow()

    init {
        getMovie(movieId?: -1)
    }

    private fun getSimilarMovies(movie: Movie){
        movie.similarVideoList.let {
            it.map {movieId->
                viewModelScope.launch {
                    _detailsState.update {detailsState->
                        detailsState.copy(isLoading = true)
                    }

                    movieListRepository.getMovieFromLocal(movieId).collectLatest { result->

                        when(result){
                            is Resource.Error ->{
                                _detailsState.update {detailsState->
                                    detailsState.copy(isLoading = false)
                                }
                            }

                            is Resource.Loading ->{
                                _detailsState.update {detailsState->
                                    detailsState.copy(isLoading = result.isLoading)
                                }
                            }

                            is Resource.Success ->{
                                result.data.let {movie->
                                    _detailsState.value.similarMovieList.add(movie!!)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getMovie(id: Int){
        viewModelScope.launch {
            _detailsState.update {
                it.copy(isLoading = true)
            }

            movieListRepository.getMovieFromLocal(id).collectLatest { result->
                when(result){
                    is Resource.Error -> {
                        _detailsState.update {
                            it.copy(isLoading = false)
                        }
                    }
                    is Resource.Loading -> {
                        _detailsState.update {
                            it.copy(isLoading = result.isLoading)
                        }
                    }
                    is Resource.Success -> {
                        result.data?.let {movie->
                            getSimilarMovies(movie)

                            _detailsState.update {
                                it.copy(movie = movie)
                            }
                        }
                    }
                }

            }
        }
    }



}