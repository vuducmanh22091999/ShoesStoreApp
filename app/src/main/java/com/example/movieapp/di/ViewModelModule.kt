package com.example.movieapp.di

import com.example.movieapp.ui.detail.cast.DetailCastViewModel
import com.example.movieapp.ui.detail.movie.DetailMovieViewModel
import com.example.movieapp.ui.favorite.FavoriteViewModel
import com.example.movieapp.ui.home.HomeViewModel
import com.example.movieapp.ui.search.SearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { HomeViewModel(get()) }
    viewModel { DetailMovieViewModel(get()) }
    viewModel { DetailCastViewModel(get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { FavoriteViewModel(get()) }
}

