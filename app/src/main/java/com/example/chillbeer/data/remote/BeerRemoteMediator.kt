package com.example.chillbeer.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.chillbeer.data.local.BeerDB
import com.example.chillbeer.data.local.BeerEntity
import com.example.chillbeer.data.mappers.toBeerEntity
import retrofit2.HttpException
import java.io.IOException

/* This class puts our loaded items from the API into our local DB and then juts forwards the page
that we want to load */

@OptIn(ExperimentalPagingApi::class)
class BeerRemoteMediator(
    private val beerDB: BeerDB, // local data source
    private val beerApi: BeerApi // remote data source
) : RemoteMediator<Int, BeerEntity>() {

    // load function is called when there is loading in pagination
    override suspend fun load(
        loadType: LoadType, // loadType can be like refresh
        state: PagingState<Int, BeerEntity>
    ): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                    if (lastItem == null) {
                        1
                    } else {
                        // calculate next page for loading
                        (lastItem.id / state.config.pageSize) + 1
                    }
                }
            }

            // make API Call
            val beers = beerApi.getBeers(
                page = loadKey,
                pageCount = state.config.pageSize
            )

            // take the list of beers and insert it in our localDB
            beerDB.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    // clear the whole cache
                    beerDB.dao.clearAll()
                }
                val beerEntities = beers.map { it.toBeerEntity() }
                beerDB.dao.upsertAll(beerEntities)
            }

            MediatorResult.Success(
                endOfPaginationReached = beers.isEmpty()
            )
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}
