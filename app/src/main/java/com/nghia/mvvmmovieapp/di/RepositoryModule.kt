package com.nghia.mvvmmovieapp.di

import com.nghia.mvvmmovieapp.movieList.data.repository.MovieListRepositoryImplement
import com.nghia.mvvmmovieapp.movieList.domain.repository.MovieListRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindMovieRepository(
        movieListRepositoryImpl: MovieListRepositoryImplement
    ): MovieListRepository
}