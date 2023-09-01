package com.example.chillbeer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.chillbeer.data.local.BeerEntity
import com.example.chillbeer.data.mappers.toBeer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/* Role of pager in constructor is to provide a flow for the loaded entries and then to load the
next ones */

@HiltViewModel
class BeerViewModel @Inject constructor(
    pager: Pager<Int, BeerEntity>
) : ViewModel() {

    /* This flow will trigger a new emission of paging data every time we scroll and we want
    to trigger loading the next page */

    val beerPagingFlow = pager
        .flow
        .map { pagingData ->
            pagingData.map { it.toBeer() }
        }
        .cachedIn(viewModelScope)
}
