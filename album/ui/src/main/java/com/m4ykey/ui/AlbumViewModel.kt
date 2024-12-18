package com.m4ykey.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.m4ykey.core.Constants.PAGE_SIZE
import com.m4ykey.core.views.BaseViewModel
import com.m4ykey.data.domain.model.album.AlbumDetail
import com.m4ykey.data.domain.model.album.AlbumItem
import com.m4ykey.data.domain.model.track.TrackItem
import com.m4ykey.data.domain.repository.AlbumRepository
import com.m4ykey.data.domain.repository.TrackRepository
import com.m4ykey.data.local.model.AlbumEntity
import com.m4ykey.data.local.model.IsAlbumSaved
import com.m4ykey.data.local.model.IsListenLaterSaved
import com.m4ykey.data.local.model.relations.AlbumWithStates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val repository: AlbumRepository,
    private val trackRepository: TrackRepository
) : BaseViewModel() {

    private var _search = MutableStateFlow<List<AlbumItem>>(emptyList())
    val search: StateFlow<List<AlbumItem>> get() = _search

    private var _newRelease = MutableStateFlow<List<AlbumItem>>(emptyList())
    val newRelease: StateFlow<List<AlbumItem>> get() = _newRelease

    private var _detail = MutableStateFlow<AlbumDetail?>(null)
    val detail: StateFlow<AlbumDetail?> get() = _detail

    private var _tracks = MutableStateFlow<List<TrackItem>>(emptyList())
    val tracks: StateFlow<List<TrackItem>> get() = _tracks

    private var _albumPaging = MutableLiveData<List<AlbumEntity>>()
    val albumPaging: LiveData<List<AlbumEntity>> get() = _albumPaging

    private var _searchResult = MutableLiveData<List<AlbumEntity>>()
    val searchResult: LiveData<List<AlbumEntity>> get() = _searchResult

    private val _isLoadingTracks = MutableStateFlow(false)
    val isLoadingTracks: StateFlow<Boolean> get() = _isLoadingTracks

    private var _totalTrackDurationMs = MutableStateFlow(0L)
    val totalTracksDuration: StateFlow<Long> = _totalTrackDurationMs

    private var offset = 0

    fun getAlbumDetails(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val albumTracks = async { getAlbumTracks(id) }
                val albumDetail = async { getAlbumById(id) }

                albumDetail.await()
                albumTracks.await()

            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getAlbumTracks(id: String) {
        if (_isLoadingTracks.value || isPaginationEnded) return

        viewModelScope.launch {
            _isLoadingTracks.value = true
            _error.value = null

            try {
                trackRepository.getAlbumTracks(offset = offset, limit = PAGE_SIZE, id = id)
                    .collect { tracks ->
                        if (tracks.isEmpty()) {
                            isPaginationEnded = true
                        } else {
                            _tracks.value += tracks
                            offset += PAGE_SIZE
                            isPaginationEnded = tracks.size < PAGE_SIZE

                            val totalDuration = tracks.sumOf { it.durationMs.toLong() }
                            _totalTrackDurationMs.value += totalDuration
                        }
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "An unknown error occurred"
            } finally {
                _isLoadingTracks.value = false
            }
        }
    }

    private fun getAlbumById(id: String) = viewModelScope.launch {
        _isLoading.value = true

        try {
            _error.value = null

            repository.getAlbumById(id).collect { result ->
                _detail.value = result
            }
        } catch (e: Exception) {
            _error.value = e.message ?: "An unknown error occurred"
        } finally {
            _isLoading.value = false
        }
    }

    fun getNewReleases() {
        if (_isLoading.value || isPaginationEnded) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                repository.getNewReleases(offset, PAGE_SIZE).collect { albums ->
                    if (albums.isEmpty()) {
                        isPaginationEnded = true
                    } else {
                        _newRelease.value += albums
                        offset += PAGE_SIZE
                        isPaginationEnded = albums.size < PAGE_SIZE
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "An unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchAlbums(query: String) {
        if (_isLoading.value || isPaginationEnded) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                repository.searchAlbums(query = query, offset = offset, limit = PAGE_SIZE)
                    .collect { albums ->
                        if (albums.isEmpty()) {
                            isPaginationEnded = true
                        } else {
                            _search.value += albums
                            offset += PAGE_SIZE
                            isPaginationEnded = albums.size < PAGE_SIZE
                        }
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "An unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetSearch() {
        offset = 0
        _isLoading.value = false
        _error.value = null
        isPaginationEnded = false
        _search.value = emptyList()
    }

    fun searchAlbumByName(albumName: String) {
        viewModelScope.launch {
            _searchResult.value = repository.searchAlbumByName(albumName)
        }
    }

    fun searchAlbumsListenLater(albumName: String) {
        viewModelScope.launch {
            _searchResult.value = repository.searchAlbumsListenLater(albumName)
        }
    }

    fun getAlbumType(albumType: String) {
        viewModelScope.launch {
            _albumPaging.value = repository.getAlbumType(albumType)
        }
    }

    fun getListenLaterCount(): Flow<Int> = repository.getListenLaterCount()

    suspend fun getRandomAlbum(): AlbumEntity? = repository.getRandomAlbum()

    fun getSavedAlbums() {
        viewModelScope.launch {
            _albumPaging.value = repository.getSavedAlbums()
        }
    }

    fun getSavedAlbumAsc() {
        viewModelScope.launch {
            _albumPaging.value = repository.getSavedAlbumAsc()
        }
    }

    fun getAlbumSortedByName() {
        viewModelScope.launch {
            _albumPaging.value = repository.getAlbumSortedByName()
        }
    }

    fun getListenLaterAlbums() {
        viewModelScope.launch {
            _albumPaging.value = repository.getListenLaterAlbums()
        }
    }

    suspend fun insertAlbum(album: AlbumEntity) = repository.insertAlbum(album)

    suspend fun insertSavedAlbum(isAlbumSaved: IsAlbumSaved) =
        repository.insertSavedAlbum(isAlbumSaved)

    suspend fun insertListenLaterAlbum(isListenLaterSaved: IsListenLaterSaved) =
        repository.insertListenLaterAlbum(isListenLaterSaved)

    suspend fun getAlbum(albumId: String): AlbumEntity? = repository.getAlbum(albumId)

    suspend fun getSavedAlbumState(albumId: String): IsAlbumSaved? =
        repository.getSavedAlbumState(albumId)

    suspend fun getListenLaterState(albumId: String): IsListenLaterSaved? =
        repository.getListenLaterState(albumId)

    suspend fun getAlbumWithStates(albumId: String): AlbumWithStates? =
        repository.getAlbumWithStates(albumId)

    suspend fun deleteAlbum(albumId: String) = repository.deleteAlbum(albumId)

    suspend fun deleteSavedAlbumState(albumId: String) = repository.deleteSavedAlbumState(albumId)

    suspend fun deleteListenLaterState(albumId: String) = repository.deleteListenLaterState(albumId)
}