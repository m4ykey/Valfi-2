package com.m4ykey.data.domain.repository

import androidx.paging.PagingData
import com.m4ykey.core.network.Resource
import com.m4ykey.data.domain.model.album.AlbumDetail
import com.m4ykey.data.domain.model.album.AlbumItem
import com.m4ykey.data.domain.model.track.TrackItem
import com.m4ykey.data.local.model.AlbumEntity
import com.m4ykey.data.local.model.ListenLaterEntity
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {

    fun searchAlbums(query : String) : Flow<PagingData<AlbumItem>>
    suspend fun getAlbumById(id : String) : Flow<Resource<AlbumDetail>>
    fun getAlbumTracks(id : String) : Flow<PagingData<TrackItem>>

    fun getAlbumSortedAlphabetical() : Flow<PagingData<AlbumEntity>>
    fun getAllAlbumsPaged() : Flow<PagingData<AlbumEntity>>
    fun getAlbumsOfTypeAlbumPaged() : Flow<PagingData<AlbumEntity>>
    fun getAlbumsOfTypeEPPaged() : Flow<PagingData<AlbumEntity>>
    fun getAlbumsOfTypeSinglePaged() : Flow<PagingData<AlbumEntity>>
    fun getAlbumsOfTypeCompilationPaged() : Flow<PagingData<AlbumEntity>>
    suspend fun getLocalAlbumById(albumId : String) : Flow<AlbumEntity>
    fun searchAlbumsByName(searchQuery : String) : Flow<PagingData<AlbumEntity>>

    suspend fun getListenLaterAlbumById(albumId : String) : Flow<ListenLaterEntity>
    fun getListenLaterCount() : Flow<Int>
    fun getListenLaterAlbums() : Flow<PagingData<ListenLaterEntity>>
    suspend fun getRandomAlbum() : ListenLaterEntity?

    suspend fun saveAlbum(album : AlbumEntity)
    suspend fun deleteAlbum(album : AlbumEntity)
    suspend fun saveListenLater(album : ListenLaterEntity)
    suspend fun deleteListenLater(album : ListenLaterEntity)

}