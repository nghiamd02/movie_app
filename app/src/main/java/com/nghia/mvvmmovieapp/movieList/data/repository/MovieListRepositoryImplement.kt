package com.nghia.mvvmmovieapp.movieList.data.repository

import com.nghia.mvvmmovieapp.movieList.data.local.movie.MovieDatabase
import com.nghia.mvvmmovieapp.movieList.data.mapper.toMovie
import com.nghia.mvvmmovieapp.movieList.data.mapper.toMovieEntity
import com.nghia.mvvmmovieapp.movieList.data.remote.MovieApi
import com.nghia.mvvmmovieapp.movieList.domain.model.Movie
import com.nghia.mvvmmovieapp.movieList.domain.repository.MovieListRepository
import com.nghia.mvvmmovieapp.util.Category
import com.nghia.mvvmmovieapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class MovieListRepositoryImplement @Inject constructor(
    private val movieApi: MovieApi,
    private val movieDatabase: MovieDatabase
): MovieListRepository {
    override suspend fun getMovieList(
        forceFetchFromRemote: Boolean,
        category: String,
        page: Int
    ): Flow<Resource<List<Movie>>> {
        return flow {
            emit(Resource.Loading(true))

            val localMovieList = movieDatabase.movieDao.getMovieListByCategory(category)

            //if localMovieList is not empty and load remote -> false
            val shouldLoadLocalMovie = localMovieList.isNotEmpty() && !forceFetchFromRemote

            //load from local database if true
            if (shouldLoadLocalMovie){
                emit(
                    Resource.Success(
                    data = localMovieList.map {movieEntity ->
                        movieEntity.toMovie(category)
                    }
                ))

                emit(Resource.Loading(false))
                return@flow
            }

            //false -> load from remote (first time)
            val movieListFromApi = try{
                movieApi.getMovieList(category, page)
            }catch (e: IOException){
                e.printStackTrace()
                emit(Resource.Error(message = "IO Error loading movies"))
                return@flow
            }catch (e: HttpException){
                e.printStackTrace()
                emit(Resource.Error(message = "HTTP Error loading movies"))
                return@flow
            }catch (e: Exception){
                e.printStackTrace()
                emit(Resource.Error(message = "Error loading movies"))
                return@flow
            }


            //Store the fetched data into local database
            val movieEntities = movieListFromApi.results.let {
                it.map {movieDto ->
                    movieDto.similarMovieList = getSimilarMoviesFromApi(movieDto.id!!, page)
                    movieDto.toMovieEntity(category)
                }
            }

            movieDatabase.movieDao.upsertMovieList(movieEntities)
            emit(
                Resource.Success(
                movieEntities.map {
                    it.toMovie(category)
                }
            ))
            emit(Resource.Loading(false))
        }
    }

    private suspend fun getSimilarMoviesFromApi(id: Int, page: Int): List<Int>
    {
         val similarMovieFromApi = try {
             movieApi.getSimilarMovieList(id, page)
         }catch (e: Exception){
             e.printStackTrace()
             return emptyList()
         }

        val similarMovieEntities = similarMovieFromApi.results.let {
            it.map { movieDto ->
                movieDto.toMovieEntity(Category.SIMILAR)
            }
        }

        movieDatabase.movieDao.upsertMovieList(similarMovieEntities)

        val similarMovieList: List<Int> = similarMovieFromApi.results.let {
            it.map {movieDto ->
                movieDto.id!!
            }
        }

        return similarMovieList
    }



    override suspend fun getMovieFromLocal(
        id: Int
    ): Flow<Resource<Movie>> {
        return flow {
            emit(Resource.Loading(false))

            val movieEntity = movieDatabase.movieDao.getMovieById(id)

            if (movieEntity != null){

                emit(
                    Resource.Success(
                        movieEntity.toMovie(movieEntity.category)
                    )
                )
                emit(Resource.Loading(false))
                return@flow
            }

            emit(Resource.Error("Error no such movie"))
            emit(Resource.Loading(false))
        }
    }
}